package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class AppSettings : Application() {
    var isDarkMode: Boolean = false

    override fun onCreate() {
        super.onCreate()
        isDarkMode = checkMode()
        themeToggle(isDarkMode)
    }

    fun checkMode(): Boolean{
        val sPref = getSharedPreferences(APP_SETTINGS_FILENAME, Application.MODE_PRIVATE)
        return (sPref.getBoolean(IS_DARK_THEME_KEY, false))
    }

    fun themeToggle(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            when {
                isDarkMode -> AppCompatDelegate.MODE_NIGHT_YES
                !isDarkMode -> AppCompatDelegate.MODE_NIGHT_NO
                else -> {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            }
        )
    }
}