package com.example.playlistmaker.domain.repository

interface SettingsRepository {
    fun isDarkMode(): Boolean
    fun setDarkMode(enabled: Boolean)
}