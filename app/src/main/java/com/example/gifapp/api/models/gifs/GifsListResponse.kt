package com.example.gifapp.api.models.gifs

import com.example.gifapp.api.models.gifs.gifItem.GifItem

class GifsListResponse(
    val data: ArrayList<GifItem>,
    val pagination: PaginationData,
    val meta: GifMetadata
)