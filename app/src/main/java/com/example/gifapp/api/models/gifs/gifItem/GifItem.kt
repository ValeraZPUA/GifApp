package com.example.gifapp.api.models.gifs.gifItem

class GifItem(
    val id: String,
    val title: String,
    val images: ImagesData,
    var isDeleted: Boolean = false
)
