package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.SettingsRepository

class SetThemeSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(enabled: Boolean) {
        settingsRepository.setDarkMode(enabled)
    }
}