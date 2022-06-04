package ru.netology.diploma.application

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
//import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.work.RefreshEventsWorker
//import ru.netology.diploma.work.RefreshJobsWorker
import ru.netology.diploma.work.RefreshPostsWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), Configuration.Provider {
    private val appScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var auth: AppAuth

    @Inject
    lateinit var workManager: Lazy<WorkManager>

    @Inject
    lateinit var workerFactory: Lazy<HiltWorkerFactory>


    override fun onCreate() {
        super.onCreate()
        setupAuth()
        setupWorkPosts()
        setupWorkEvents()
    }

    private fun setupWorkPosts() {
        appScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<RefreshPostsWorker>(1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            workManager.get().enqueueUniquePeriodicWork(
                RefreshPostsWorker.name,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    private fun setupWorkEvents() {
        appScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<RefreshEventsWorker>(1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            workManager.get().enqueueUniquePeriodicWork(
                RefreshEventsWorker.name,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    private fun setupAuth() {
        appScope.launch {
            auth.sendPushToken()
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory.get())
            .build()
    }
}