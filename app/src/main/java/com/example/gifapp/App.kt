package com.example.gifapp

import android.app.Application
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.example.gifapp.di.AppComponent
import com.example.gifapp.di.DaggerAppComponent
import com.example.gifapp.di.modules.DatabaseModule
import com.example.gifapp.di.modules.NetworkModule
import com.example.gifapp.models.StateRepository

class App : Application() {

    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        PRDownloader.initialize(this, PRDownloaderConfig.newBuilder()
            .build())

        appComponent = DaggerAppComponent.builder()
            .networkModule(NetworkModule())
            .databaseModule(DatabaseModule(this))
            .stateRepository(StateRepository())
            .build()
    }
}