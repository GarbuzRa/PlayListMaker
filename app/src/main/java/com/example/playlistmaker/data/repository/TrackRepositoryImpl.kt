package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.remote.ItunesApiService
import com.example.playlistmaker.data.storage.SharedPreferencesStorage
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import retrofit2.Callback
import retrofit2.Response

class TrackRepositoryImpl(val apiService: ItunesApiService, val storage: SharedPreferencesStorage):TrackRepository {
    override fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit) {
        apiService.search(query).enqueue(object : Callback<ItunesApiService.SearchResponse> {
            override fun onResponse(
                call: retrofit2.Call<ItunesApiService.SearchResponse>,
                response: Response<ItunesApiService.SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results?.map { it.toDomainModel() } ?: emptyList()
                    callback(Result.success(tracks))
                } else {
                    callback(Result.failure(Exception("API error: ${response.code()}")))
                }
            }

            override fun onFailure(call: retrofit2.Call<ItunesApiService.SearchResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    override fun getSearchHistory(): List<Track> = storage.readTracks()

    override fun addToSearchHistory(track: Track) {
        val history = getSearchHistory().toMutableList()
        history.removeIf { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > 10) {
            history.subList(0, 10)
        }
        storage.saveTracks(history)
    }

    override fun clearSearchHistory() {
        storage.clearTracks()
    }

    private fun ItunesApiService.TrackDto.toDomainModel() = Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )

}