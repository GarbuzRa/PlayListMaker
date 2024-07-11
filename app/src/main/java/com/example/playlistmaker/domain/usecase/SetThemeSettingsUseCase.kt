package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.SettingsRepository

class SetThemeSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(isDarkMode: Boolean) {
        settingsRepository.setDarkMode(isDarkMode)
    }
}