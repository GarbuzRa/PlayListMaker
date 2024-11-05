package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectedPlaylistViewModel(
    private val playlistsInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<PlayList>()
    val playlist: LiveData<PlayList> = _playlist

    private val _playlistTracks = MutableLiveData<List<Track>>()
    val playlistTracks: LiveData<List<Track>> = _playlistTracks

    private val _trackCount = MutableLiveData<Int>()
    val trackCount: LiveData<Int> = _trackCount

    private val _playlistTime = MutableLiveData<Long>()
    val playlistTime: LiveData<Long> = _playlistTime

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadPlaylist(playlistId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val playlist = playlistsInteractor.getPlaylistById(playlistId)
                _playlist.value = playlist
                loadPlaylistTracks(playlist.tracksId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadPlaylistTracks(trackIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val tracks = playlistsInteractor.getAllTracks(trackIds)
            withContext(Dispatchers.Main) {
                _playlistTracks.value = tracks
                _trackCount.value = tracks.size
                calculatePlaylistTime()
            }
        }
    }

    private fun calculatePlaylistTime() {
        val time = _playlistTracks.value?.sumOf { it.trackTimeMillis ?: 0 } ?: 0
        _playlistTime.value = time.toLong()
    }

    fun deleteTrackFromPlaylist(trackId: Long) {
        val currentPlaylist = _playlist.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            playlistsInteractor.deleteTrackFromPlaylist(currentPlaylist.id, trackId)
            playlistsInteractor.trackCountDecrement(currentPlaylist.id)
            loadPlaylist(currentPlaylist.id)
        }
    }

    fun deletePlaylist() {
        val currentPlaylistId = _playlist.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            playlistsInteractor.deletePlaylistById(currentPlaylistId)
        }
    }
}