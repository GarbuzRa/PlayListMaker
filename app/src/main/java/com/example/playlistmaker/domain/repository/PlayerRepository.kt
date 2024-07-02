package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

//интерфейс функций работы с плеером
//ф-ии взяли из PlayerActivity

interface PlayerRepository {

    fun preparePlayer(track: Track)//ф-ия подготовки* плеера на основе трека
    //*подготовка - это запись ссылки на mp3 в класс медиаПлеер

    fun playTrack() //ф-ия запуска трека
    fun pauseTrack()//ф-ия остановки трека

    fun releasePlayer()//ф-ия прекращения работы плеера

    fun isPlaying():Boolean//играет ли трек сейчас

    fun getCurrentPosition():Int//текущее время воспроизведения
}