package com.example.gifapp.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gif_items")
data class GifItemEntity(
    @PrimaryKey
    val id: String,
    val gif_title: String,
    val image_url: String,
    var is_deleted: Boolean
)