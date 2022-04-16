package ru.netology.diploma.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.dto.Ad
import ru.netology.diploma.dto.FeedItem
import ru.netology.diploma.model.FeedModelState
import ru.netology.diploma.repository.PostRepository
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
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


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAllPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }
}

