package com.example.gifapp.di

import com.example.gifapp.utils.DataManager
import com.example.gifapp.di.modules.DatabaseModule
import com.example.gifapp.di.modules.NetworkModule
import com.example.gifapp.utils.StateRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        DatabaseModule::class,
        StateRepository::class
    ]
)

interface AppComponent {
    fun inject(testClass: DataManager)
}