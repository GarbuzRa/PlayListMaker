package com.example.playlistmaker.data.remote

import com.example.playlistmaker.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface ItunesApiService {
    @GET("/search?entity=song")
   suspend fun search(@Query("term") text: String): SearchResponse

    data class SearchResponse(
        val resultCount: Int,
        val results: MutableList<TrackDto>
    )

    data class TrackDto(
        val trackId: String,
        val trackName: String,
        val artistName: String,
        val trackTimeMillis: Int,
        val artworkUrl100: String, //ссылка на обложку
        val collectionName: String,
        val releaseDate: String,
        val primaryGenreName: String,
        val country: String,
        val previewUrl: String
    )
}