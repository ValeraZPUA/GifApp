package com.example.gifapp.models.stateRepository

import android.annotation.SuppressLint
import com.example.gifapp.db.entities.GifItemEntity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@SuppressLint("CheckResult")
class StateRepository {

    private var previousKeyWord: String = ""
    private var offset: Int = 0
    private val gifList: ArrayList<GifItemEntity> = arrayListOf()
    private var isInterConnected = true

    init {
        ReactiveNetwork
            .observeInternetConnectivity()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::setIsInternetConnected)
    }
    fun addGifs(newGifList: ArrayList<GifItemEntity>) {
        newGifList.also {
            gifList.addAll(it)

            if (it.isEmpty())
                resetOffset()
        }
    }

    fun setKeyWord(newKeyWord: String) {
        previousKeyWord = newKeyWord
    }

    fun getGigList(): ArrayList<GifItemEntity> {
        return ArrayList(gifList.filter { !it.is_deleted })
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

    fun increaseOffset(offset: Int) {
        this.offset += offset
    }

    fun getOffset(): Int {
        return offset
    }

    fun deleteGifById(gifId: String) {
        gifList
            .find { it.id == gifId }
            .also { gifList.remove(it) }
    }

    private fun resetOffset() {
        offset = 0
    }
}