package ru.netology.diploma.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import ru.netology.diploma.entity.PostWorkEntity

@Dao
interface PostWorkDao {

    @Query("SELECT * From PostWorkEntity WHERE localId = :id")
    suspend fun getById(id: Long): PostWorkEntity


    @Insert(onConflict = REPLACE)
    suspend fun insert(work: PostWorkEntity): Long

    @Query("DELETE FROM PostWorkEntity WHERE localId = :id")
    suspend fun removeById(id: Long)

}
