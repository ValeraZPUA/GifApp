package com.example.gifapp.models

interface DataRepo {

    fun getGifList(keyWord: String?, offset: Int?)

}