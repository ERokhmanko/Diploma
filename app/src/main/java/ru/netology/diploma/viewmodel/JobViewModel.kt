package ru.netology.diploma.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.dto.Job
import ru.netology.diploma.model.JobModel
import ru.netology.diploma.model.JobsModelState
import ru.netology.diploma.repository.JobRepository
import javax.inject.Inject


@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
//    private val stateHandle: SavedStateHandle,
     appAuth: AppAuth
) : ViewModel() {

    val data: LiveData<JobModel> = repository.data
        .map { job ->
            JobModel(
                job,
                job.isEmpty()
            )
        }.asLiveData(Dispatchers.Default)

    val id = appAuth.authStateFlow.value.id
    val job = MutableLiveData<List<Job>>()

    private val _dataState = MutableLiveData<JobsModelState>()
    val dataState: LiveData<JobsModelState>
        get() = _dataState

    init {
        getJobsByUserId(id)
//        getUserById(stateHandle.get(USER_ID) ?: 0)
    }


    private fun getJobsByUserId(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = JobsModelState(loading = true)
            job.value = repository.getJobsByUserId(id)
            _dataState.value = JobsModelState()
        } catch (e: Exception) {
            _dataState.value = JobsModelState(error = true)
        }
    }
}

