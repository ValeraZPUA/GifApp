package com.example.gifapp.ui.fragments.gifLIst

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gifapp.utils.DataManager
import com.example.gifapp.db.entities.GifItemEntity

open class GifListViewModel(app: Application) : AndroidViewModel(app) {

    private val _gifsData = MutableLiveData<ArrayList<GifItemEntity>>()
    val gifsData: LiveData<ArrayList<GifItemEntity>>
        get() = _gifsData

    var dataManager: DataManager = DataManager(_gifsData, app.cacheDir.absolutePath + "/gif_app_cache/")

    open fun getGifs(keyWord: String?, offset: Int?) {
        dataManager.getGifs(keyWord, offset)
    }

    open fun setDeleted(gifId: String) {
        dataManager.setDeleted(gifId)
    }
}