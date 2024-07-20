// AppSettings.kt
package com.example.playlistmaker.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.di.dataModule
import com.example.playlistmaker.di.domainModule
import com.example.playlistmaker.di.viewModelModule
import com.example.playlistmaker.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.domain.usecase.SetThemeSettingsUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class AppSettings : Application(), KoinComponent {
    private val getThemeSettingsUseCase: GetThemeSettingsUseCase by inject()
    private val setThemeSettingsUseCase: SetThemeSettingsUseCase by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@AppSettings)
            modules(dataModule, domainModule, viewModelModule)
        }

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