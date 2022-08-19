package ru.netology.diploma.viewmodel

import android.net.Uri
import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.diploma.R
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.dto.*
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.enumeration.EventType
import ru.netology.diploma.enumeration.RetryType
import ru.netology.diploma.model.EventFormState
import ru.netology.diploma.model.EventModelState
import ru.netology.diploma.model.FileModel
import ru.netology.diploma.repository.EventRepository
import ru.netology.diploma.utils.SingleLiveEvent
import ru.netology.diploma.work.RemoveEventWorker
import ru.netology.diploma.work.SaveEventWorker
import java.time.Instant
import javax.inject.Inject
import kotlin.random.Random


@RequiresApi(Build.VERSION_CODES.O)
val emptyEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    datetime = Instant.now().toString(),
    published = Instant.now().toString(),
    coords = null,
    type = EventType.ONLINE,
    likeOwnerIds = mutableSetOf(),
    likedByMe = false,
    speakerIds = mutableSetOf(),
    participantsIds = mutableSetOf(),
    participatedByMe = false,
    attachment = null,
    link = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val workManager: WorkManager,
    appAuth: AppAuth,
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        repository.data.map { event ->
            event.map {
                it.copy(
                    ownedByMe = it.authorId == myId,
                    participatedByMe = it.participantsIds.contains(myId),
                    likedByMe = it.likeOwnerIds.contains(myId)
                )
            }.filter { validEvent ->
                !hideEvents.contains(validEvent)
            }
        }
            .map { pagingData ->
                pagingData.insertSeparators(
                    generator = { before, _ ->
                        if (before?.id?.rem(5) != 0L) null
                        else Ad(
                            Random.nextLong(),
                            "https://ik.imagekit.io/dndjiayeehz/5384119566_BzAHtLM9x.jpg?ik-sdk-version=javascript-1.4.3&updatedAt=1651659074287"
                        )
                    }
                )
            }
    }.flowOn(Dispatchers.Default)

    private val _dataState = MutableLiveData<EventModelState>()
    val dataState: LiveData<EventModelState>
        get() = _dataState

    private val _edited = MutableLiveData(emptyEvent)
    val edited: LiveData<Event>
        get() = _edited

    private var _speakersId: MutableSet<Long> = mutableSetOf()

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val noFile = FileModel()
    private val _file = MutableLiveData(noFile)
    val file: LiveData<FileModel>
        get() = _file

    private val hideEvents = mutableSetOf<Event>()

    private val _eventForm = MutableLiveData<EventFormState>()
    val eventFormState: LiveData<EventFormState>
        get() = _eventForm

    fun save() {
        _edited.value?.let {
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    val id = repository.saveWork(
                        it, _file.value?.uri?.let { MediaUpload(it) }, _file.value?.type
                    )
                    val data = workDataOf(SaveEventWorker.eventKey to id)
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val request = OneTimeWorkRequestBuilder<SaveEventWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
                    workManager.enqueue(request)

                    _dataState.value = EventModelState()
                    _file.value = noFile
                    _edited.value = emptyEvent
                } catch (e: Exception) {
                    _dataState.value = EventModelState(error = true)
                }
            }
        }

    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            val data = workDataOf(RemoveEventWorker.eventKey to id)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<RemoveEventWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(request)

            _dataState.value = EventModelState()
        } catch (e: Exception) {
            _dataState.value =
                EventModelState(error = true, retryType = RetryType.REMOVE, retryId = id)
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likedEventById(id)
        } catch (e: Exception) {
            _dataState.value =
                EventModelState(error = true, retryType = RetryType.LIKE, retryId = id)
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            repository.unlikedEventById(id)
        } catch (e: Exception) {
            _dataState.value =
                EventModelState(error = true, retryType = RetryType.UNLIKE, retryId = id)
        }
    }

    fun changeContent(
        dateTime: String,
        format: EventType,
        link: String?,
        coord: Coordinates?,
        content: String,
    ) {

        _edited.value?.let {
            val linkEvent = link?.trim()
            val contentEvent = content.trim()

            if (it.datetime != dateTime ||
                it.type != format ||
                it.link != linkEvent ||
                it.coords != coord ||
                it.content != contentEvent
            )

                _edited.value = it.copy(
                    datetime = dateTime,
                    type = format,
                    link = linkEvent,
                    coords = coord,
                    content = contentEvent
                )
        }
    }

    fun participateById(id: Long) = viewModelScope.launch {
        try {
            repository.participateById(id)
        } catch (e: Exception) {
            _dataState.value =
                EventModelState(error = true, retryType = RetryType.PARTICIPATE, retryId = id)
        }
    }

    fun unParticipateById(id: Long) = viewModelScope.launch {
        try {
            repository.unParticipateById(id)
        } catch (e: Exception) {
            _dataState.value =
                EventModelState(error = true, retryType = RetryType.UNPARTICIPATE, retryId = id)
        }
    }

    fun changeFile(uri: Uri?, type: AttachmentType?) {
        _file.value = FileModel(uri, type)

        _edited.value?.let {
            _edited.value = it.copy(
                attachment = null
            )
        }
    }

    fun edit(event: Event) {
        _edited.value = event
    }

    fun hideEvent(event: Event) {
        hideEvents.add(event)
    }

    fun isLinkValid(link: String) {
        val linkTrim = link.trim()
        if (!Patterns.WEB_URL.matcher(linkTrim).matches()) {
            _eventForm.value = EventFormState(linkError = R.string.invalid_link)
        } else {
            _eventForm.value = EventFormState(isDataValid = true)
        }
    }

    fun requireData(
        date: String,
        time: String,
        link: String,
        coord: String,
        content: String,
        format: EventType
    ) {

        _eventForm.value = EventFormState(
            emptyDateError = if (date.isBlank()) R.string.empty_field else null,
            emptyTimeError = if (time.isBlank()) R.string.empty_field else null,
            emptyLinkError = if (format == EventType.ONLINE && link.isBlank()) R.string.empty_field else null,
            emptyCoordError = if (format == EventType.OFFLINE && coord.isBlank()) R.string.empty_field else null,
            emptyContentError = if (content.isBlank()) R.string.empty_field else null,
            isDataNotBlank = if (format == EventType.ONLINE) {
                date.isNotBlank() &&
                        time.isNotBlank() &&
                        content.isNotBlank() &&
                        link.isNotBlank()
            } else {
                date.isNotBlank() &&
                        time.isNotBlank() &&
                        content.isNotBlank() &&
                        coord.isNotBlank()
            }
        )

    }

    fun checkSpeaker(id: Long) {
        _speakersId.let {
            if (it.contains(id))
                it.remove(id)
            else
                it.add(id)
        }
    }

    fun isCheckboxSpeaker(id: Long): Boolean {
        return _speakersId.any { it == id }
    }

    fun saveSpeakers() {
        _edited.value = edited.value?.copy(speakerIds = _speakersId)
        clear()
    }

    fun clear() {
        _speakersId = mutableSetOf()
    }

    fun saveCoord(latitude: Double, longitude: Double) {
        _edited.value = edited.value?.copy(coords = Coordinates(latitude, longitude))
    }

}

