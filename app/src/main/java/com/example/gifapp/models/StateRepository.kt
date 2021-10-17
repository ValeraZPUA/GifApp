package com.example.gifapp.models

import android.annotation.SuppressLint
import com.example.gifapp.db.entities.GifItemEntity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Singleton

@SuppressLint("CheckResult")
@Module
class StateRepository {

    private var previousKeyWord: String = ""
    private val gifList: ArrayList<GifItemEntity> = arrayListOf()
    private var isInterConnected = true

    init {
        ReactiveNetwork
            .observeInternetConnectivity()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::setIsInternetConnected)
    }

    @Singleton
    @Provides
    fun initStateRepo(): StateRepository {
        return this
    }

    fun addGifs(newGifList: ArrayList<GifItemEntity>) {
        gifList.addAll(newGifList)
    }

    fun setKeyWord(newKeyWord: String) {
        previousKeyWord = newKeyWord
    }

    fun getGigList(): ArrayList<GifItemEntity> {
        return gifList
    }

    fun getOffset(): Int {
        return gifList.size
    }

    fun getKeyWord(): String {
        return previousKeyWord
    }

    fun getIsInternetConnected(): Boolean {
        return isInterConnected
    }

    private fun setIsInternetConnected(isConnected: Boolean) {
        isInterConnected = isConnected
    }
}