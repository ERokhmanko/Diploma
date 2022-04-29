package ru.netology.diploma.viewmodel

import androidx.lifecycle.*
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.dto.Job
import ru.netology.diploma.dto.MediaUpload
import ru.netology.diploma.enumeration.RetryType
import ru.netology.diploma.model.JobModel
import ru.netology.diploma.model.JobsModelState
import ru.netology.diploma.model.PostsModelState
import ru.netology.diploma.repository.JobRepository
import ru.netology.diploma.utils.SingleLiveEvent
import ru.netology.diploma.work.RemoveJobWorker
import ru.netology.diploma.work.RemovePostWorker
import ru.netology.diploma.work.SaveJobWorker
import ru.netology.diploma.work.SavePostWorker
import javax.inject.Inject

val emptyJob = Job(
    id = 0,
    name = "",
    position = "",
    start = 0L,
    finish = null,
    link = "",
)

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
//    private val stateHandle: SavedStateHandle,
    private val workManager: WorkManager,
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

    private val edited = MutableLiveData(emptyJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    init {
        getJobsByUserId(id)
//        getUserById(stateHandle.get(USER_ID) ?: 0)
    }

    fun loadJobs() = viewModelScope.launch {
        try {
            _dataState.value = JobsModelState(loading = true)
            repository.getMyJobs()
            _dataState.value = JobsModelState()
        } catch (e: Exception) {
            _dataState.value = JobsModelState(error = true)
        }
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

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            val data = workDataOf(RemoveJobWorker.postKey to id)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<RemovePostWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(request)

            _dataState.value = JobsModelState()
        } catch (e: Exception) {
            _dataState.value =
                JobsModelState(error = true, retryType = RetryType.REMOVE, retryId = id)
        }
    }

    fun edit(job: Job) {
        edited.value = job
    }

    fun changeData(start: Long, finish: Long, company: String, website: String, position: String) {
        edited.value?.let {
            val textCompany = company.trim()
            val textWebsite = website.trim()
            val textPosition = position.trim()
            if (it.start != start ||
                it.finish != finish ||
                it.name != textCompany ||
                it.link != textWebsite ||
                it.position != textPosition
            )
                edited.value = it.copy(
                    start = start,
                    finish = finish,
                    name = textCompany,
                    link = textWebsite,
                    position = textPosition
                )
        }
    }

    fun save() {
        edited.value?.let {
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    val id = repository.saveWork(it)
                    val data = workDataOf(SaveJobWorker.postKey to id)
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val request = OneTimeWorkRequestBuilder<SaveJobWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
                    workManager.enqueue(request)

                    _dataState.value = JobsModelState()
                } catch (e: Exception) {
                    _dataState.value = JobsModelState(error = true)
                }
            }
        }
    }
}

