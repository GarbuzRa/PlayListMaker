package com.example.playlistmaker.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlayerRepository

class PlayerRepositoryImpl(private var mediaPlayer: MediaPlayer?):PlayerRepository {

    override fun preparePlayer(track: Track) { //ф-ия подготовки* плеера на основе трека
        //*подготовка - это запись ссылки на mp3 в класс медиаПлеер
        mediaPlayer?.release() // release() - очистить если не нулл
        mediaPlayer = MediaPlayer().apply { //создаем медиаПлеер и применяем настройки (.apply)
            setDataSource(track.previewUrl) // setDataSource - установить ссылку на mp3
            prepare() //применить
        }
    }

    override fun playTrack() {
        mediaPlayer?.start() //запускаем если не нулл
    }

    override fun pauseTrack() {
        mediaPlayer?.pause() //ставим на паузу если не нулл
    }

    override fun releasePlayer() {
        mediaPlayer?.release() //очистили если не нулл
        mediaPlayer = null //как только очистили он стал нулл
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false //вернуть переменную isPlaying
        //если mediaPlayer не null, вернуть false если null
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0 //вернуть переменную CurrentPosition
        //если mediaPlayer не null, вернуть 0 если null
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer?.setOnCompletionListener { listener() }
    }

}