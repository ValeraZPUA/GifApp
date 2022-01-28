package com.example.gifapp.baseClasses

import androidx.lifecycle.*
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.dataManager.DataManager
import com.example.gifapp.models.dataManager.IDataManager

open class BaseViewModel(
    private val dataManager: DataManager
    ) : ViewModel(), IDataManager.ViewModel, LifecycleObserver {

    private val gifsData = MutableLiveData<ArrayList<GifItemEntity>>()
    private val showInternetConnectionError = MutableLiveData<Boolean>()

    fun getGifs(keyWord: String?, offset: Int?) {
        val isInternetConnected = dataManager.getIsInternetConnected()
        dataManager.setIsInternetConnected(isInternetConnected)
        if (!isInternetConnected) {
            showInternetConnectionError.value = isInternetConnected
        }
        dataManager.getGifList(keyWord, offset)
    }

    fun getGifList(): ArrayList<GifItemEntity> {
        return dataManager.getGifListFromStateRepository()
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