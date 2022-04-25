package ru.netology.diploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.diploma.dao.*
import ru.netology.diploma.entity.*

@Database(
    entities = [PostEntity::class,
        PostRemoteKeyEntity::class, UserEntity::class, JobEntity::class, PostWorkEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
    abstract fun postWorkDao(): PostWorkDao
}
