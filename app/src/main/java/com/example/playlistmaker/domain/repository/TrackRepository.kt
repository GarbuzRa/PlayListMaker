package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

//интерфейс который определяет ф-ии взаимодейсвтия с данными треков
//действия взяли из SearchActivity

interface TrackRepository{

    fun searchTracks(query: String): Flow<Result<List<Track>>>


    fun getSearchHistory():List<Track> //получить историю поиска
    fun addToSearchHistory(track: Track) //добавить в историю поиска
    fun clearSearchHistory() //очистить историю поиска

}