package com.example.gifapp.ui.fragments.oneGif

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gifapp.App
import com.example.gifapp.BuildConfig
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.utils.DataManager
import javax.inject.Inject

class OneGifViewModel(app: Application) : AndroidViewModel(app) {

    private val _gifsData = MutableLiveData<ArrayList<GifItemEntity>>()
    val gifsData: LiveData<ArrayList<GifItemEntity>>
        get() = _gifsData

    @Inject
    lateinit var dataManager: DataManager

    init {
        App.appComponent.inject(this)
        dataManager.initRequiredData(_gifsData, app.cacheDir.absolutePath + BuildConfig.CACHE_DIR)
    }

    fun getGifsList(): ArrayList<GifItemEntity> {
        return dataManager.getGifList()
    }

    fun getGifs(keyWord: String?, offset: Int?) {
        dataManager.getGifs(keyWord, offset)
    }
}