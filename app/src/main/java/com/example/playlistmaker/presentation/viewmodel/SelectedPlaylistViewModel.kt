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


class SelectedPlaylistViewModel(private val playlistsInteractor: PlaylistInteractor) : ViewModel() {

    private lateinit var tracks: List<Track>

    private val _playlistId = MutableLiveData<PlayList>()
    fun observePlaylistId(): LiveData<PlayList> = _playlistId

    private val _playlistTracks = MutableLiveData<List<Track>>()
    fun observePlaylistTracks(): LiveData<List<Track>> = _playlistTracks

    private val _trackCount = MutableLiveData<Int>()
    fun observeTrackCount(): LiveData<Int> = _trackCount

    private val _playlistTime = MutableLiveData<Long>()
    fun observePlaylistAllTime(): LiveData<Long> = _playlistTime


    fun playlistAllTime() {
        if (_playlistTracks.value != null) {
            var time: Long = 0
            _playlistTracks.value?.forEach {
                time += it.trackTimeMillis ?: 0
            }
            _playlistTime.postValue(time)
        }
    }

    fun getPlaylistById(playlistId: Int) {
        viewModelScope.launch {
            _playlistId.postValue(playlistsInteractor.getPlaylistById(playlistId))
        }
    }

    fun getAllTracks(playlistId: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            tracks = playlistsInteractor.getAllTracks(playlistId)
            _playlistTracks.postValue(tracks)
        }
    }

    fun deleteTrackFromPlaylist(playlist: PlayList, trackId: Long) {
        val playlistId = _playlistId.value?.id
        viewModelScope.launch(Dispatchers.IO) {
            playlist.trackCount = playlist.tracksId.size - 1
            if (playlistId != null) {
                playlistsInteractor.deleteTrackFromPlaylist(playlistId, trackId)
                trackCountDecrease(playlistId)
                updatePlaylist(playlistId)
            }
        }
    }

    fun deletePlaylist() {
        val playlistId = _playlistId.value?.id
        viewModelScope.launch(Dispatchers.IO) {
            if (playlistId != null) {
                playlistsInteractor.deletePlaylistById(playlistId)
            }
        }
    }

    private fun updatePlaylist(playlistId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val update = playlistsInteractor.getPlaylistById(playlistId)
            _playlistId.postValue(update)
            val updateTracks = playlistsInteractor.getAllTracks(update.tracksId)
            _playlistTracks.postValue(updateTracks)
            _trackCount.postValue(updateTracks.size)
        }
    }

    private suspend fun trackCountDecrease(playlistId: Int) {
        playlistsInteractor.trackCountDecrement(playlistId)
    }
}