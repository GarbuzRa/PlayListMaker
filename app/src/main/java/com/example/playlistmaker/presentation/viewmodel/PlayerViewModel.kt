package com.example.playlistmaker.presentation.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.usecase.ReleasePlayerUseCase
import com.example.playlistmaker.domain.usecase.GetTrackUseCase
import com.example.playlistmaker.domain.usecase.PlayTrackUseCase
import com.example.playlistmaker.domain.usecase.PauseTrackUseCase
import com.example.playlistmaker.domain.usecase.PrepareTrackUseCase
import com.example.playlistmaker.util.Creator
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlayerViewModel(
    private val getTrackUseCase: GetTrackUseCase,
    private val playTrackUseCase: PlayTrackUseCase,
    private val pauseTrackUseCase: PauseTrackUseCase,
    private val prepareTrackUseCase: PrepareTrackUseCase,
    private val releasePlayerUseCase: ReleasePlayerUseCase
) : ViewModel() {

    private val _trackData = MutableLiveData<TrackUiState>()
    val trackData: LiveData<TrackUiState> = _trackData

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData<String>()
    val currentPosition: LiveData<String> = _currentPosition

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeTask = object : Runnable {
        override fun run() {
            updateCurrentPosition()
            if (_isPlaying.value == true) {
                handler.postDelayed(this, 500L)
            }
        }
    }

    fun preparePlayer(trackId: String) {
        val track = getTrackUseCase(trackId)
        if(track != null) prepareTrackUseCase(track)
        track?.let {
            _trackData.value = TrackUiState(
                trackName = it.trackName,
                artistName = it.artistName,
                trackTimeMillis = SimpleDateFormat("mm:ss", Locale.getDefault()).format(it.trackTimeMillis),
                artworkUrl = it.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"),
                collectionName = it.collectionName,
                releaseDate = LocalDateTime.parse(it.releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).year.toString(),
                primaryGenreName = it.primaryGenreName,
                country = it.country
            )
        }
        _currentPosition.value = "00:00"
    }

    fun playbackControl() {
        if (_isPlaying.value == true) {
            pauseTrackUseCase()
            _isPlaying.value = false
            handler.removeCallbacks(updateTimeTask)
        } else {
            playTrackUseCase()
            _isPlaying.value = true
            handler.post(updateTimeTask)
        }
    }

    private fun updateCurrentPosition() {
        val position = Creator.providePlayerRepository().getCurrentPosition()
        _currentPosition.value = SimpleDateFormat("mm:ss", Locale.getDefault()).format(position)
    }

    fun releasePlayer() {
        releasePlayerUseCase()
        handler.removeCallbacks(updateTimeTask)
    }

    fun onPause() {
        if (_isPlaying.value == true) {
            pauseTrackUseCase()
            _isPlaying.value = false
            handler.removeCallbacks(updateTimeTask)
        }
    }

    data class TrackUiState(
        val trackName: String,
        val artistName: String,
        val trackTimeMillis: String,
        val artworkUrl: String,
        val collectionName: String,
        val releaseDate: String,
        val primaryGenreName: String,
        val country: String
    )
}