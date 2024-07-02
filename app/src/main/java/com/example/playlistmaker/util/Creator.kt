package com.example.playlistmaker.util

import android.content.Context
import com.example.playlistmaker.app.AppSettings
import com.example.playlistmaker.data.remote.ItunesApiService
import com.example.playlistmaker.data.repository.PlayerRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.data.storage.SharedPreferencesStorage
import com.example.playlistmaker.domain.repository.PlayerRepository
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.usecase.GetTrackUseCase
import com.example.playlistmaker.domain.usecase.PauseTrackUseCase
import com.example.playlistmaker.domain.usecase.PlayTrackUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val itunesApiService: ItunesApiService by lazy {
        retrofit.create(ItunesApiService::class.java)
    }

    private val sharedPreferencesStorage: SharedPreferencesStorage by lazy {
        SharedPreferencesStorage(appContext.getSharedPreferences("app_preferences", Context.MODE_PRIVATE))
    }

    private val trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(itunesApiService, sharedPreferencesStorage)
    }

    private val playerRepository: PlayerRepository by lazy {
        PlayerRepositoryImpl()
    }

    fun provideTrackRepository(context: Context): TrackRepository {
        return trackRepository
    }

    fun providePlayerRepository(): PlayerRepository {
        return playerRepository
    }

    fun provideGetTrackUseCase(): GetTrackUseCase {
        return GetTrackUseCase(trackRepository)
    }

    fun providePlayTrackUseCase(): PlayTrackUseCase {
        return PlayTrackUseCase(playerRepository)
    }

    fun providePauseTrackUseCase(): PauseTrackUseCase {
        return PauseTrackUseCase(playerRepository)
    }

    fun provideAppSettings(): AppSettings {
        return appContext as AppSettings
    }
}