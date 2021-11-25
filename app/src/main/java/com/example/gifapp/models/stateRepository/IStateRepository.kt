package com.example.gifapp.models.stateRepository

import com.example.gifapp.db.entities.GifItemEntity

interface IStateRepository {
    fun clearGifList()
    fun getGifList(): ArrayList<GifItemEntity>
    fun getIsInternetConnected(): Boolean
    fun setKeyWord(newKeyWord: String)
    fun getKeyWord(): String
    fun getOffset(): Int
    fun increaseOffset(offset: Int)
    fun addGifs(newGifList: ArrayList<GifItemEntity>)
    fun deleteGifById(gifId: String)
}