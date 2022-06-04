package ru.netology.diploma.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import ru.netology.diploma.entity.JobWorkEntity

@Dao
interface JobWorkDao {

    @Query("SELECT * From JobWorkEntity WHERE localId = :id")
    suspend fun getById(id: Long): JobWorkEntity


    @Insert(onConflict = REPLACE)
    suspend fun insert(work: JobWorkEntity): Long

    @Query("DELETE FROM JobWorkEntity WHERE localId = :id")
    suspend fun removeById(id: Long)

}
