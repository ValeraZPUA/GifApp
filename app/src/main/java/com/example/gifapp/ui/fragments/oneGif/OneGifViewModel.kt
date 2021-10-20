package com.example.gifapp.ui.fragments.oneGif

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gifapp.App
import com.example.gifapp.BuildConfig
import com.example.gifapp.R
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.DataManager
import javax.inject.Inject

class OneGifViewModel(private val app: Application) : AndroidViewModel(app) {

    private val gifsData = MutableLiveData<ArrayList<GifItemEntity>>()

    @Inject
    lateinit var dataManager: DataManager

    init {
        App.appComponent.inject(this)
        dataManager.initRequiredData(this.gifsData, app.cacheDir.absolutePath + BuildConfig.CACHE_DIR)
    }

    fun getGifsList(): ArrayList<GifItemEntity> {
        return dataManager.getGifList()
    }

    fun getGifs(keyWord: String?, offset: Int?) {
        val isInternetConnected = dataManager.getIsInternetConnected()
        if (!isInternetConnected) {
            Toast.makeText(app, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
        }
        dataManager.getGifs(keyWord, offset, isInternetConnected)
    }

    fun getGifsData(): LiveData<ArrayList<GifItemEntity>> {
        return gifsData
    }
}