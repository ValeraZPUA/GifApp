package com.example.gifapp.di

import com.example.gifapp.di.modules.DatabaseModule
import com.example.gifapp.di.modules.NetworkModule
import com.example.gifapp.ui.fragments.gifLIst.GifListViewModel
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
    fun inject(gifListViewModel: GifListViewModel)
}