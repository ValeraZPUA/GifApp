package com.example.gifapp.di.modules

import android.content.Context
import androidx.room.Room
import com.example.gifapp.db.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule(private val applicationContext: Context) {

    @Singleton
    @Provides
    fun roomInit() : AppDatabase {
        return Room.databaseBuilder(applicationContext, AppDatabase::class.java, "gif_database")
            .build()
    }

}