package ru.netology.diploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.diploma.dao.*
import ru.netology.diploma.entity.*

@Database(
    entities = [PostEntity::class,
        PostRemoteKeyEntity::class, UserEntity::class, JobEntity::class, PostWorkEntity::class,
        JobWorkEntity::class, EventEntity::class, EventWorkEntity::class, EventRemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun postWorkDao(): PostWorkDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun eventWorkDao(): EventWorkDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
    abstract fun jobWorkDao(): JobWorkDao
}
