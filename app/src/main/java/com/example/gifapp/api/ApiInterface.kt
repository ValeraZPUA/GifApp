package com.example.gifapp.api

import com.example.gifapp.BuildConfig
import com.example.gifapp.api.models.gifs.GifsListResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("v1/gifs/search")
    fun getGifs(@Query("q") keyWord: String,
                @Query("offset") offset: Int,
                @Query("limit") itemQuantityToGet: Int = 4,
                @Query("api_key") apiKey: String = BuildConfig.API_KEY): Observable<GifsListResponse>

}