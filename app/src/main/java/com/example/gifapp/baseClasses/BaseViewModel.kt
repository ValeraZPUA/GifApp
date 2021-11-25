package com.example.gifapp.baseClasses

import androidx.lifecycle.*
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.dataManager.DataManager
import com.example.gifapp.models.dataManager.IDataManager

open class BaseViewModel(private val dataManager: DataManager) : ViewModel(), IDataManager, LifecycleObserver {

    private val gifsData = MutableLiveData<ArrayList<GifItemEntity>>()
    private val showInternetConnectionError = MutableLiveData<Boolean>()

    fun getGifs(keyWord: String?, offset: Int?) {
        val isInternetConnected = dataManager.getIsInternetConnected()
        if (!isInternetConnected) {
            showInternetConnectionError.value = true
        }
        dataManager.getGifs(keyWord, offset, isInternetConnected)
    }

    fun getGifList(): ArrayList<GifItemEntity> {
        return dataManager.getGifList()
    }

    fun getGifsData(): LiveData<ArrayList<GifItemEntity>> {
        return gifsData
    }

    fun getIsInternetConnectionError(): LiveData<Boolean> {
        return showInternetConnectionError
    }

    fun initRequiredData(dirPathFromSavingCache: String) {
        dataManager.initRequiredData(dirPathFromSavingCache, this)
    }

    override fun returnGifList(gifList: ArrayList<GifItemEntity>) {
        gifsData.value = gifList
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stopAllRxRequests() {
        dataManager.stopAllRxRequests()
    }
}