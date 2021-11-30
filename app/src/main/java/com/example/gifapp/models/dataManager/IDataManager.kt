package com.example.gifapp.models.dataManager

import com.example.gifapp.db.entities.GifItemEntity

interface IDataManager {
    interface ViewModel {
        fun returnGifList(gifList: ArrayList<GifItemEntity>)
    }

    interface Helper {
        fun updateStateRepositoryList(gifList: ArrayList<GifItemEntity>)
        fun returnGifList(gifList: ArrayList<GifItemEntity>)
        fun increaseStateRepositoryOffset(offsetSize: Int)
        fun downloadGifTOInternalStorage(imageUrl: String, imageTitle: String)
        fun deleteGifFromStateRepositoryById(gifId: String)
        fun checkDeletedGifs(gifList: ArrayList<GifItemEntity>)
    }
}