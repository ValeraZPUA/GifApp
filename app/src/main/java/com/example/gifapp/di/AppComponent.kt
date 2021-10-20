package com.example.gifapp.di

import com.example.gifapp.models.DataManager
import com.example.gifapp.di.modules.DatabaseModule
import com.example.gifapp.di.modules.NetworkModule
import com.example.gifapp.ui.fragments.gifLIst.GifListViewModel
import com.example.gifapp.ui.fragments.oneGif.OneGifViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        DatabaseModule::class
    ]
)

interface AppComponent {
    fun inject(dataManager: DataManager)
    fun inject(gifListViewModel: GifListViewModel)
    fun inject(oneGifViewModel: OneGifViewModel)
}