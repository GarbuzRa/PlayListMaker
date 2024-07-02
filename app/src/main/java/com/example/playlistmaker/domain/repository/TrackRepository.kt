package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track
//интерфейс который определяет ф-ии взаимодейсвтия с данными треков
//действия взяли из SearchActivity

interface TrackRepository{

    fun searchTracks(query: String, callback:(Result<List<Track>>) -> Unit)
    //ф-ия поиска треков, которая принимает запрос(query) и действие при ответе на запрос(callback)
    //callback типа данных Unit, и уже сама ф-ия Unit принимает в себя результат листа треков
    //Result - сериализующий класс, в данном случае в лист треков.

    fun getSearchHistory():List<Track> //получить историю поиска
    fun addToSearchHistory(track: Track) //добавить в историю поиска
    fun clearSearchHistory() //очистить историю поиска

}