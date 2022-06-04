package ru.netology.diploma.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.diploma.dao.*
import ru.netology.diploma.db.AppDb
import ru.netology.diploma.dto.Job
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): AppDb =
        Room.databaseBuilder(context, AppDb::class.java, "app.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providesPostDao(appDb: AppDb): PostDao = appDb.postDao()

    @Provides
    fun providesPostRemoteKeyDao(appDb: AppDb): PostRemoteKeyDao = appDb.postRemoteKeyDao()

    @Provides
    fun providesPostWorkDao(appDb: AppDb) : PostWorkDao = appDb.postWorkDao()

    @Provides
    fun providesEventDao(appDb: AppDb): EventDao = appDb.eventDao()

    @Provides
    fun providesEventRemoteKeyDao(appDb: AppDb): EventRemoteKeyDao = appDb.eventRemoteKeyDao()

    @Provides
    fun providesEventWorkDao(appDb: AppDb) : EventWorkDao = appDb.eventWorkDao()

    @Provides
    fun providesUserDao(appDb: AppDb): UserDao = appDb.userDao()

    @Provides
    fun providesJobDao(appDb: AppDb): JobDao = appDb.jobDao()

    @Provides
    fun providesJobWorkDao(appDb: AppDb) : JobWorkDao = appDb.jobWorkDao()
}