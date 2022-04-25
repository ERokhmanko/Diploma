package ru.netology.diploma.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.dto.*
import ru.netology.diploma.model.FileModel
import ru.netology.diploma.model.PostsModelState
import ru.netology.diploma.repository.PostRepository
import ru.netology.diploma.utils.SingleLiveEvent
import ru.netology.diploma.work.SavePostWorker
import java.io.File
import javax.inject.Inject
import kotlin.random.Random


val emptyPost = Post(
    id = 0,
    content = "",
    author = "",
    authorId = 0,
    authorAvatar = "",
    likedByMe = false,
    published = "",
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
            post.map { it.copy(ownedByMe = it.authorId == myId) }
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

}

