package com.example.gifapp.models.stateRepository

import com.example.gifapp.db.entities.GifItemEntity

class StateRepository : IStateRepository {

    private var previousKeyWord: String = ""
    private var offset: Int = 0
    private val gifList: ArrayList<GifItemEntity> = arrayListOf()

    override fun addGifs(newGifList: ArrayList<GifItemEntity>) {
        newGifList.also {
            gifList.addAll(it)

            if (it.isEmpty())
                resetOffset()
        }
    }

    override fun setKeyWord(newKeyWord: String) {
        previousKeyWord = newKeyWord
    }

    override fun getGifList(): ArrayList<GifItemEntity> {
        return ArrayList(gifList.filter { !it.is_deleted })
    }

    override fun getKeyWord(): String {
        return previousKeyWord
    }

    override fun increaseOffset(offset: Int) {
        this.offset += offset
    }

    override fun getOffset(): Int {
        return offset
    }

    override fun deleteGifById(gifId: String) {
        gifList
            .find { it.id == gifId }
            .also { gifList.remove(it) }
    }

    private fun resetOffset() {
        offset = 0
    }

    override fun clearGifList() {
        gifList.clear()
    }
}