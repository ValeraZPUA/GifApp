package com.example.gifapp.ui.fragments.gifLIst

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.dataManager.DataManager
import com.example.gifapp.models.dataManager.IDataManager
import javax.inject.Inject

class GifListViewModel @Inject constructor(private val dataManager: DataManager) : ViewModel(), IDataManager {

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

    fun setDeleted(gifId: String) {
        dataManager.setDeleted(gifId)
    }

    override fun returnGifList(gifList: ArrayList<GifItemEntity>) {
        gifsData.value = gifList
    }
}