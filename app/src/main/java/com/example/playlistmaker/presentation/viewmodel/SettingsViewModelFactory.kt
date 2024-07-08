package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.domain.usecase.SetThemeSettingsUseCase
import com.example.playlistmaker.util.Creator

class SettingsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val sharedPreferences = Creator.provideSharedPreferences()
            val settingsRepository = SettingsRepositoryImpl(sharedPreferences)
            val getThemeSettingsUseCase = GetThemeSettingsUseCase(settingsRepository)
            val setThemeSettingsUseCase = SetThemeSettingsUseCase(settingsRepository)
            return SettingsViewModel(getThemeSettingsUseCase, setThemeSettingsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}