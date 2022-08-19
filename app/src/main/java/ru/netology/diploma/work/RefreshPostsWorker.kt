package ru.netology.diploma.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.netology.diploma.repository.PostRepository

@HiltWorker
class RefreshPostsWorker @AssistedInject constructor(
    private val repository: PostRepository,
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val name = "ru.netology.work.RefreshPostsWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {

        try {
            repository.getLatest()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

}



