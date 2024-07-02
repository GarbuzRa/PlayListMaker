package com.example.playlistmaker.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.example.playlistmaker.domain.model.Track

class SharedPreferencesStorage(private val sharedPreferences: SharedPreferences) {
    fun readTracks(): List<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY_KEY, null) ?: return emptyList()
        return Gson().fromJson(json, Array<Track>::class.java).toList()
    }

    fun saveTracks(tracks: List<Track>) {
        val json = Gson().toJson(tracks)
        sharedPreferences.edit().putString(TRACK_HISTORY_KEY, json).apply()
    }

    fun clearTracks() {
        sharedPreferences.edit().remove(TRACK_HISTORY_KEY).apply()
    }

    companion object {
        private const val TRACK_HISTORY_KEY = "track_history_key"
    }
}