// AppSettings.kt
package com.example.playlistmaker.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.util.Creator
import com.example.playlistmaker.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.domain.usecase.SetThemeSettingsUseCase

class AppSettings : Application() {
    private lateinit var getThemeSettingsUseCase: GetThemeSettingsUseCase
    private lateinit var setThemeSettingsUseCase: SetThemeSettingsUseCase

    override fun onCreate() {
        super.onCreate()
        Creator.init(this) // Initialize the Creator here
        getThemeSettingsUseCase = Creator.provideGetThemeSettingsUseCase()
        setThemeSettingsUseCase = Creator.provideSetThemeSettingsUseCase()

        val isDarkMode = getThemeSettingsUseCase.execute()
        themeToggle(isDarkMode)
    }

    fun themeToggle(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        setThemeSettingsUseCase.execute(isDarkMode)
    }
}
