package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.domain.usecase.SetThemeSettingsUseCase

class SettingsViewModel(
    private val getThemeSettingsUseCase: GetThemeSettingsUseCase,
    private val setThemeSettingsUseCase: SetThemeSettingsUseCase
) : ViewModel() {

    private val _isDarkMode = MutableLiveData<Boolean>()
    val isDarkMode: LiveData<Boolean> = _isDarkMode

    init {
        _isDarkMode.value = getThemeSettingsUseCase.execute()
    }

    fun setDarkMode(enabled: Boolean) {
        setThemeSettingsUseCase.execute(enabled)
        _isDarkMode.value = enabled
    }
}
