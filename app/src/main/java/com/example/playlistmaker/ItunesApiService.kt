package com.example.playlistmaker


import retrofit2.Call
import retrofit2.http.GET

interface ItunesApiService {
    @GET("/search?entity=song")
    fun search(text: String): Call<SearchResponse>
}