package ru.netology.diploma.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.diploma.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE authorId = :id ORDER BY id DESC")
    fun getPagingSource(id: Long): PagingSource<Int, PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE PostEntity SET likedByMe = 1 WHERE id = :id AND likedByMe = 0")
    suspend fun likedById(id: Long)

    @Query("UPDATE PostEntity SET likedByMe = 0 WHERE id = :id AND likedByMe = 1")
    suspend fun unlikedById( id: Long)
}
