package ru.netology.diploma.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import ru.netology.diploma.entity.EventWorkEntity

@Dao
interface EventWorkDao {

    @Query("SELECT * From EventWorkEntity WHERE localId = :id")
    suspend fun getById(id: Long): EventWorkEntity


    @Insert(onConflict = REPLACE)
    suspend fun insert(work: EventWorkEntity): Long

    @Query("DELETE FROM EventWorkEntity WHERE localId = :id")
    suspend fun removeById(id: Long)

}
