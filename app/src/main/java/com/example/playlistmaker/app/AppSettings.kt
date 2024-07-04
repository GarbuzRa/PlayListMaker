package com.example.playlistmaker.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.util.APP_SETTINGS_FILENAME
import com.example.playlistmaker.util.Creator
import com.example.playlistmaker.util.IS_DARK_THEME_KEY

class AppSettings : Application() {
    var isDarkMode: Boolean = false //дарк мод не включен

    override fun onCreate() {
        super.onCreate()
        Creator.init(this) //вызываем функцию инит (которая сохраняет контекст) ЗДЕСЬ
        isDarkMode = checkMode() //проверяет что там с дарк модом (включен он или нет)
        themeToggle(isDarkMode) //не очень понимаю что делает эта ф-ия. Типа переключает тему?
    }

    private fun checkMode(): Boolean {
        val sPref = getSharedPreferences(APP_SETTINGS_FILENAME, MODE_PRIVATE) //получает файл SP по ключу
        return sPref.getBoolean(IS_DARK_THEME_KEY, false) //возвращает значение
    }

    fun themeToggle(isDarkMode: Boolean) { //хз
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}