package ru.netology.diploma.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ru.netology.diploma.repository.EventRepository


@HiltWorker
class SaveEventWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: EventRepository
) : CoroutineWorker(appContext, params) {
    companion object {
        const val eventKey = "event"
    }

    override suspend fun doWork(): Result {
        val id = inputData.getLong(eventKey, 0L)
        if (id == 0L) {
            return Result.failure()
        }

        return try {
            repository.processWork(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

