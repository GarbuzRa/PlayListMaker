package com.example.playlistmaker.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.util.APP_SETTINGS_FILENAME
import com.example.playlistmaker.util.Creator
import com.example.playlistmaker.util.IS_DARK_THEME_KEY

class AppSettings : Application() {
    var isDarkMode: Boolean = false

    override fun onCreate() {
        super.onCreate()
        Creator.init(this)
        isDarkMode = checkMode()
        themeToggle(isDarkMode)
    }

    private fun checkMode(): Boolean {
        val sPref = getSharedPreferences(APP_SETTINGS_FILENAME, MODE_PRIVATE)
        return sPref.getBoolean(IS_DARK_THEME_KEY, false)
    }

    fun themeToggle(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}