package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.usecase.GetCurrentPositionUseCase
import com.example.playlistmaker.domain.usecase.ReleasePlayerUseCase
import com.example.playlistmaker.domain.usecase.GetTrackUseCase
import com.example.playlistmaker.domain.usecase.PlayTrackUseCase
import com.example.playlistmaker.domain.usecase.PauseTrackUseCase
import com.example.playlistmaker.domain.usecase.PrepareTrackUseCase
import com.example.playlistmaker.domain.usecase.SetOnCompletionListenerUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
    private val setOnCompletionListenerUseCase: SetOnCompletionListenerUseCase,
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _trackData = MutableLiveData<TrackUiState>()
    val trackData: LiveData<TrackUiState> = _trackData

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData<String>()
    val currentPosition: LiveData<String> = _currentPosition

    private val _currentTrack = MutableLiveData<Track>()
    var currentTrack : LiveData<Track> = _currentTrack

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite : LiveData<Boolean> = _isFavorite

    private val _playlists = MutableLiveData<List<PlayList>>()
    val playlists: LiveData<List<PlayList>> = _playlists

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private var updateJob: Job? = null

    fun isInPlaylist(playlist: PlayList, trackId: Long): Boolean {
        var data = false
        for (track in playlist.tracksId) {
            if (track == trackId) data = true
        }
        return data
    }

    fun onAddToPlaylistClick(playlist: PlayList, track: Track) {
        viewModelScope.launch {
            playlist.trackCount = playlist.tracksId.size + 1
            playlistInteractor.insertTrackToPlaylist(playlist, track)
        }
    }

    fun preparePlayer(trackId: String) {
        _currentTrack.value = getTrackUseCase(trackId)!!
        val track = _currentTrack.value
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
        _currentTrack.value = track!!
    }

    init {
        viewModelScope.launch {
            val favoritesIds = favoritesInteractor.getFavoritesIds().first()
            _isFavorite.value = favoritesIds.contains(_currentTrack.value!!.trackId)
            _currentTrack.value!!.isFavorite = _isFavorite.value!!
        }
        refreshPlaylists()
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

    fun onFavoriteClicked(){
        val track = _currentTrack.value ?: return
        viewModelScope.launch {
            if (track.isFavorite){
                favoritesInteractor.deleteFavorite(track)
            }else{
                favoritesInteractor.insertFavorite(track)
            }
            track.isFavorite = !track.isFavorite
            _isFavorite.postValue(track.isFavorite)
        }
    }

    fun refreshPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { playlistsList ->
                _playlists.value = playlistsList
            }
        }
    }

    fun addTrackToPlaylist(playlist: PlayList) {
        viewModelScope.launch {
            val track = _currentTrack.value ?: return@launch
            try {
                playlistInteractor.insertTrackToPlaylist(playlist, track)
                _message.value = "Добавлено в плейлист ${playlist.name}"
            } catch (e: Exception) {
                if (e is IllegalStateException && e.message == "Track already exists in playlist") {
                    _message.value = "Трек уже добавлен в плейлист ${playlist.name}"
                } else {
                    _message.value = "Ошибка при добавлении трека в плейлист"
                }
            }
        }
    }

}