package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.repository.SettingsRepository
import com.example.playlistmaker.util.IS_DARK_THEME_KEY

class SettingsRepositoryImpl(private val sharedPreferences: SharedPreferences) : SettingsRepository {
    override fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(IS_DARK_THEME_KEY, false)
    }

    override fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(IS_DARK_THEME_KEY, enabled).apply()
    }
}