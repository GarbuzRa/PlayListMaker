package com.example.playlistmaker.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.example.playlistmaker.domain.model.Track

class SharedPreferencesStorage(private val sharedPreferences: SharedPreferences) {
    fun readTracks(): List<Track> { //ф-ия которая читает все треки (имеющиеся), отдает массив из треков
        //ключ к данному SP хранится в файле globals
        val json = sharedPreferences.getString(TRACK_HISTORY_KEY, null) ?: return emptyList()
        //на 10 строке создан экземпляр SP, который получает строку по названию файла...
        //если в нем ничего нет, тогда возвращает просто пустой лист
        return Gson().fromJson(json, Array<Track>::class.java).toList() //возвращаем значение переведенное из
        //Json файла в класс
    }

    fun saveTracks(tracks: List<Track>) {
        val json = Gson().toJson(tracks) //переменная которая переводит из класса в json файл
        sharedPreferences.edit().putString(TRACK_HISTORY_KEY, json).apply() //логика сохранения трека в файл
    }

    fun clearTracks() {
        sharedPreferences.edit().remove(TRACK_HISTORY_KEY).apply() //просто удалить из файла SP
    }

    companion object {
        private const val TRACK_HISTORY_KEY = "track_history_key" //позволяет пользоваться этим файлом SP всем.
    }
}