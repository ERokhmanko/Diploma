package ru.netology.diploma.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.netology.diploma.repository.JobRepository
import ru.netology.diploma.repository.PostRepository

@HiltWorker
class RefreshJobsWorker @AssistedInject constructor(
    private val repository: JobRepository,
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val name = "ru.netology.work.RefreshJobsWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {

        try {
            repository.getMyJobs()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}



