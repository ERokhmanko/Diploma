package ru.netology.diploma.viewmodel

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.dto.*
import ru.netology.diploma.enumeration.RetryType
import ru.netology.diploma.model.FileModel
import ru.netology.diploma.model.PostsModelState
import ru.netology.diploma.repository.PostRepository
import ru.netology.diploma.utils.SingleLiveEvent
import ru.netology.diploma.work.RemovePostWorker
import ru.netology.diploma.work.SavePostWorker
import java.io.File
import java.time.Instant
import javax.inject.Inject
import kotlin.random.Random


@RequiresApi(Build.VERSION_CODES.O)
val emptyPost = Post(
    id = 0,
    content = "",
    author = "",
    authorId = 0,
    authorAvatar = "",
    likedByMe = false,
    published = Instant.now().toString(),
    coords = null,
    link = null,
    mentionIds = mutableSetOf(),
    mentionedMe = false,
    likeOwnerIds = emptySet(),
    attachment = null
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val workManager: WorkManager,
    private val appAuth: AppAuth
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        repository.data.map { post ->
            post.map { it.copy(ownedByMe = it.authorId == myId) }.filter { validPost ->
                !hidePosts.contains(validPost) //TODO проверить работает ли фильтрация
            }
        }
            .map { pagingData ->
                pagingData.insertSeparators(
                    generator = { before, _ ->
                        if (before?.id?.rem(5) != 0L) null
                        else Ad(
                            Random.nextLong(),
                            "image.png" //TODO подтягивается картинка с сервака? Если нет. то создать свою
                        )
                    }
                )
            }
    }.flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userWall: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.userWall(myId).map { pagingData ->
                pagingData.map { post ->
                    post.copy(
                        ownedByMe = post.authorId == myId,
                        likedByMe = post.likeOwnerIds.contains(myId)
                    )
                }
            }
        }

    private val _dataState = MutableLiveData<PostsModelState>()
    val dataState: LiveData<PostsModelState>
        get() = _dataState

    val edited = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val noFile = FileModel()
    private val _file = MutableLiveData(noFile)
    val file: LiveData<FileModel>
        get() = _file

    private val hidePosts = mutableSetOf<Post>()

    init {
        loadPosts()
    }


    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = PostsModelState(loading = true)
            repository.getAll()
            _dataState.value = PostsModelState()
        } catch (e: Exception) {
            _dataState.value = PostsModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    val id = repository.saveWork(
                        it, _file.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    val data = workDataOf(SavePostWorker.postKey to id)
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val request = OneTimeWorkRequestBuilder<SavePostWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
                    workManager.enqueue(request)

                    _dataState.value = PostsModelState()
                } catch (e: Exception) {
                    _dataState.value = PostsModelState(error = true)
                }
            }
        }

    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            val data = workDataOf(RemovePostWorker.postKey to id)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<RemovePostWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(request)

            _dataState.value = PostsModelState()
        } catch (e: Exception) {
            _dataState.value =
                PostsModelState(error = true, retryType = RetryType.REMOVE, retryId = id)
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likedPostById(id)
        } catch (e: Exception) {
            _dataState.value =
                PostsModelState(error = true, retryType = RetryType.LIKE, retryId = id)
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            repository.unlikedPostById(id)
        } catch (e: Exception) {
            _dataState.value =
                PostsModelState(error = true, retryType = RetryType.UNLIKE, retryId = id)
        }
    }

    fun changeContent(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (it.content != text)
                edited.value = it.copy(content = text)
        }
    }

    fun changeFile(uri: Uri?, file: File?) {
        _file.value = FileModel(uri, file)
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun hidePost(post: Post) {
        hidePosts.add(post)
    }


}

