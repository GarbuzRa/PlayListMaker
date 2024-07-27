package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.data.remote.ItunesApiService
import com.example.playlistmaker.data.repository.PlayerRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.data.storage.SharedPreferencesStorage
import com.example.playlistmaker.domain.repository.PlayerRepository
import com.example.playlistmaker.domain.repository.SettingsRepository
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.usecase.AddToSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.ClearSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.GetCurrentPositionUseCase
import com.example.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.domain.usecase.GetTrackUseCase
import com.example.playlistmaker.domain.usecase.PauseTrackUseCase
import com.example.playlistmaker.domain.usecase.PlayTrackUseCase
import com.example.playlistmaker.domain.usecase.PrepareTrackUseCase
import com.example.playlistmaker.domain.usecase.ReleasePlayerUseCase
import com.example.playlistmaker.domain.usecase.SearchTracksUseCase
import com.example.playlistmaker.domain.usecase.SetOnCompletionListenerUseCase
import com.example.playlistmaker.domain.usecase.SetThemeSettingsUseCase
import com.example.playlistmaker.presentation.viewmodel.FavoriteTracksViewModel
import com.example.playlistmaker.presentation.viewmodel.MediatekaViewModel
import com.example.playlistmaker.presentation.viewmodel.PlayListViewModel
import com.example.playlistmaker.presentation.viewmodel.PlayerViewModel
import com.example.playlistmaker.presentation.viewmodel.SearchViewModel
import com.example.playlistmaker.presentation.viewmodel.SettingsViewModel
import com.example.playlistmaker.util.APP_SETTINGS_FILENAME
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module{
    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<TrackRepository> { TrackRepositoryImpl(get(),get()) }

    single<ItunesApiService> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApiService::class.java)
    }
    single { androidContext().getSharedPreferences(APP_SETTINGS_FILENAME, Context.MODE_PRIVATE) }
    single { SharedPreferencesStorage(get()) }
    single { MediaPlayer() }

}

val domainModule = module{
    factory { AddToSearchHistoryUseCase(get()) }
    factory { ClearSearchHistoryUseCase(get()) }
    factory { GetCurrentPositionUseCase(get()) }
    factory { GetSearchHistoryUseCase(get()) }
    factory { GetThemeSettingsUseCase(get()) }
    factory { GetTrackUseCase(get()) }
    factory { PauseTrackUseCase(get()) }
    factory { PlayTrackUseCase(get()) }
    factory { PrepareTrackUseCase(get()) }
    factory { ReleasePlayerUseCase(get()) }
    factory { SearchTracksUseCase(get()) }
    factory { SetOnCompletionListenerUseCase(get()) }
    factory { SetThemeSettingsUseCase(get()) }
}

val viewModelModule = module{
    viewModel {MediatekaViewModel()}
    viewModel {FavoriteTracksViewModel()}
    viewModel {PlayListViewModel()}
    viewModel { PlayerViewModel(get(),get(),get(),get(),get(),get(),get())}
    viewModel { SearchViewModel(get(),get(),get(),get())}
    viewModel { SettingsViewModel(get(),get())}
}
