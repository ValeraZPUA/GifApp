package com.example.gifapp.di

import com.example.gifapp.di.modules.DatabaseModule
import com.example.gifapp.di.modules.NetworkModule
import com.example.gifapp.ui.fragments.gifLIst.GifListFragment
import com.example.gifapp.ui.fragments.oneGif.OneGifFragment
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
    fun inject(gifListFragment: GifListFragment)
    fun inject(oneGifFragment: OneGifFragment)
}