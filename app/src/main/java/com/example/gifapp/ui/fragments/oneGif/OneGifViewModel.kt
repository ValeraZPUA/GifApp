package com.example.gifapp.ui.fragments.oneGif

import android.app.Application
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.ui.fragments.gifLIst.GifListViewModel

class OneGifViewModel(app: Application) : GifListViewModel(app) {

    fun getGifsList(): ArrayList<GifItemEntity> {
        return dataManager.getGifList()
    }
}