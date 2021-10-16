package com.example.gifapp.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gifapp.db.entities.GifItemEntity
import io.reactivex.Single

@Dao
interface GifItemDao {
    @Query("SELECT is_deleted FROM gif_items WHERE id = :gifId")
    fun isGifDeleted(gifId: String): Single<Boolean>

    @Query("UPDATE gif_items SET is_deleted = :isDelete WHERE id = :gifId")
    fun setGifDeleted(isDelete: Boolean, gifId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGifs(gifLIst: List<GifItemEntity>)

    @Query("SELECT * FROM gif_items WHERE gif_title LIKE :keyWork")
    fun searchInDB(keyWork: String): Single<List<GifItemEntity>>
}