package com.example.gifapp.models.dataManager

import com.example.gifapp.db.entities.GifItemEntity

interface IDataManager {
    fun returnGifList(gifList: ArrayList<GifItemEntity>)
}