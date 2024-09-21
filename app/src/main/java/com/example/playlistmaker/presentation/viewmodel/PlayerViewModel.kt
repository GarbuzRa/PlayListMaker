package com.example.playlistmaker.presentation.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.repository.PlayerRepository
import com.example.playlistmaker.domain.usecase.GetCurrentPositionUseCase
import com.example.playlistmaker.domain.usecase.ReleasePlayerUseCase
import com.example.playlistmaker.domain.usecase.GetTrackUseCase
import com.example.playlistmaker.domain.usecase.PlayTrackUseCase
import com.example.playlistmaker.domain.usecase.PauseTrackUseCase
import com.example.playlistmaker.domain.usecase.PrepareTrackUseCase
import com.example.playlistmaker.domain.usecase.SetOnCompletionListenerUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlayerViewModel(
    private val getTrackUseCase: GetTrackUseCase,
    private val playTrackUseCase: PlayTrackUseCase,
    private val pauseTrackUseCase: PauseTrackUseCase,
    private val prepareTrackUseCase: PrepareTrackUseCase,
    private val releasePlayerUseCase: ReleasePlayerUseCase,
    private val getCurrentPositionUseCase: GetCurrentPositionUseCase,
    private val setOnCompletionListenerUseCase: SetOnCompletionListenerUseCase
) : ViewModel() {

    private val _trackData = MutableLiveData<TrackUiState>()
    val trackData: LiveData<TrackUiState> = _trackData

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData<String>()
    val currentPosition: LiveData<String> = _currentPosition

    private var updateJob: Job? = null

    fun preparePlayer(trackId: String) {
        val track = getTrackUseCase(trackId)
        if(track != null){
            prepareTrackUseCase(track)
            setOnCompletionListenerUseCase {
                _isPlaying.postValue(false)
                _currentPosition.postValue("00:00")
                updateJob?.cancel()
            }
            _trackData.value = TrackUiState(
                trackName = track.trackName,
                artistName = track.artistName,
                trackTimeMillis = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis),
                artworkUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"),
                collectionName = track.collectionName,
                releaseDate = LocalDateTime.parse(track.releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).year.toString(),
                primaryGenreName = track.primaryGenreName,
                country = track.country
            )
        }

        _currentPosition.value = "00:00"
    }

    fun playbackControl() {
        if (_isPlaying.value == true) {
            pauseTrackUseCase()
            _isPlaying.value = false
            updateJob?.cancel()
        } else {
            playTrackUseCase()
            _isPlaying.value = true
            startTimerCoroutine()
        }
    }

    fun startTimerCoroutine () {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                updateCurrentPosition()
                delay(500)
            }
        }
    }

    private fun updateCurrentPosition() {
        val position = getCurrentPositionUseCase()
        _currentPosition.value = SimpleDateFormat("mm:ss", Locale.getDefault()).format(position)
    }

    fun releasePlayer() {
        releasePlayerUseCase()
        updateJob?.cancel()
    }

    fun onPause() {
        if (_isPlaying.value == true) {
            pauseTrackUseCase()
            _isPlaying.value = false
            updateJob?.cancel()
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