package com.example.playlistmaker.util

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
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
import com.example.playlistmaker.domain.usecase.PrepareTrackUseCase
import com.example.playlistmaker.domain.usecase.ReleasePlayerUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator { //в общем я так понимаю суть данного объекта заключается в том, чтобы просто
    // создавать что-либо внутри себя, а потом предоставлять это другим
    private lateinit var appContext: Context //переменная которая принимает в себя класс Context
    // и будет инициализированна позже

    private var mediaPlayer = MediaPlayer()

    fun init(context: Context) { //ф-ия принимает в себя контекст и сохраняет его в переменную (я хз зачем)
        appContext = context.applicationContext
    }

    private val retrofit: Retrofit by lazy { //ну здесь по классике создается ретрофит для выполнения сетевых запросов
        //lazy нужен чтобы объект создавался только при первом обращении
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val itunesApiService: ItunesApiService by lazy { //создается объект сервиса при помощи ретрофита
        retrofit.create(ItunesApiService::class.java)
    }

    private val sharedPreferencesStorage: SharedPreferencesStorage by lazy { //тут будут сохраняться настройки приложения в SP
        SharedPreferencesStorage(appContext.getSharedPreferences("app_preferences", Context.MODE_PRIVATE))
    }

    private val trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(itunesApiService, sharedPreferencesStorage)
    }

    private val playerRepository: PlayerRepository by lazy {
        PlayerRepositoryImpl(mediaPlayer)
    }
    //ну вот тут просто два репозитория создается, один для плеера, другой для работы с треками

    fun provideTrackRepository(): TrackRepository {
        return trackRepository //возвращает объект
    }

    fun providePlayerRepository(): PlayerRepository {
        return playerRepository //возвращает объект
    }

    fun provideGetTrackUseCase(): GetTrackUseCase {
        return GetTrackUseCase(trackRepository) //возвращает объект
    }

    fun providePlayTrackUseCase(): PlayTrackUseCase {
        return PlayTrackUseCase(playerRepository)
    }

    fun providePauseTrackUseCase(): PauseTrackUseCase {
        return PauseTrackUseCase(playerRepository)
    }

    fun providePrepareTrackUseCase(): PrepareTrackUseCase{
        return PrepareTrackUseCase(playerRepository)
    }

    fun provideReleasePlayerUseCase(): ReleasePlayerUseCase{
        return ReleasePlayerUseCase(playerRepository)
    }

    fun provideAppSettings(): AppSettings {
        return appContext as AppSettings
    }

    fun provideSharedPreferences(): SharedPreferences {
        return appContext.getSharedPreferences(APP_SETTINGS_FILENAME, Context.MODE_PRIVATE)
    }

    fun loadImage(context: Context, url: String, imageView: ImageView, placeholder: Int, cornerRadius: Int) {
        Glide.with(context)
            .load(url)
            .placeholder(placeholder)
            .centerCrop()
            .transform(RoundedCorners(cornerRadius))
            .into(imageView)
    }


}
//короче все они просто возвращают объекты, но так как я не вижу всей картины происходящего, то ваще хз для чего