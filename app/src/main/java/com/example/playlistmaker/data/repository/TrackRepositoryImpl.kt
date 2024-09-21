package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.remote.ItunesApiService
import com.example.playlistmaker.data.storage.SharedPreferencesStorage
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class TrackRepositoryImpl(val apiService: ItunesApiService, val storage: SharedPreferencesStorage):TrackRepository {

    override fun searchTracks(query: String): Flow<Result<List<Track>>> = flow {
        try {
            val response = apiService.search(query)
            val searchResult = response.results.map { it.toDomainModel() }
            emit(Result.success(searchResult))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }




    override fun getSearchHistory(): List<Track> = storage.readTracks() //ну я так понимаю вызывает
    // ф-ию readTracks из класса SharedPreferencesStorage

    override fun addToSearchHistory(track: Track) {
        val history = getSearchHistory().toMutableList() //переводит в изменяемый лист
        history.removeIf { it.trackId == track.trackId }  //удаляет элемент из массива, если он соответсвует условиям в теле
        history.add(0, track) //добавляет трек по индексу
        if (history.size > 10) { //если размер истории больше 10..
            history.subList(0, 10) //тогда добавляет его в нулевой индекс (то есть он самый топовый будет)
        }
        storage.saveTracks(history) //вызывает ф-ию saveTracks у класса SharedPreferencesStorage
    }

    override fun clearSearchHistory() {
        storage.clearTracks() //просто вызывает команду очистки истории
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