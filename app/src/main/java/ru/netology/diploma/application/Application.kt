package ru.netology.diploma.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
//import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.diploma.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() {
        super.onCreate()
        setupAuth()
    }

    private fun setupAuth() {
        appScope.launch {
            appAuth.sendPushToken()
        }
    }
}