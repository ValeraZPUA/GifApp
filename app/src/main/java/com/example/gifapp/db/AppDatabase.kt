package com.example.gifapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gifapp.db.daos.GifItemDao
import com.example.gifapp.db.entities.GifItemEntity

@Database(entities = [GifItemEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gifItemDao(): GifItemDao
}