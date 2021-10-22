package com.example.gifapp.ui.fragments.oneGif

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.DataManager
import javax.inject.Inject

class OneGifViewModel @Inject constructor(private val dataManager: DataManager) : ViewModel() {

    private val gifsData = MutableLiveData<ArrayList<GifItemEntity>>()
    private val showInternetConnectionError = MutableLiveData<Boolean>()

    fun getGifsList(): ArrayList<GifItemEntity> {
        return dataManager.getGifList()
    }

    fun getGifs(keyWord: String?, offset: Int?) {
        val isInternetConnected = dataManager.getIsInternetConnected()
        if (!isInternetConnected) {
            showInternetConnectionError.value = true
        }
        dataManager.getGifs(keyWord, offset, isInternetConnected)
    }

    fun getGifsData(): LiveData<ArrayList<GifItemEntity>> {
        return gifsData
    }

    fun getIsInternetConnectionError(): LiveData<Boolean> {
        return showInternetConnectionError
    }

    fun initRequiredData(dirPathFromSavingCache: String) {
        dataManager.initRequiredData(this.gifsData, dirPathFromSavingCache)
    }
}