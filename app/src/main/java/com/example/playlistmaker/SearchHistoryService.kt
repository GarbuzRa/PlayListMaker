package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson

class SearchHistoryService(sharedPreferences: SharedPreferences) {

    private val sharedPreferences = sharedPreferences
    fun read(): Array<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY_KEY, null) ?: return emptyArray()
        return Gson().fromJson(json, Array<Track>::class.java)
    }

    fun clear(){
        sharedPreferences
            .edit()
            .clear()
            .apply()
    }

    fun add(newTrack: Track) {
        var tempList = read().toMutableList()

        tempList.removeIf { it.trackId == newTrack.trackId }
        tempList.add(0, newTrack)

        if (tempList.size > 10) {
            tempList = tempList.subList(0, 10)
        }

        val json = Gson().toJson(tempList)

        sharedPreferences
            .edit()
            .putString(TRACK_HISTORY_KEY, json)
            .apply()
    }
}
