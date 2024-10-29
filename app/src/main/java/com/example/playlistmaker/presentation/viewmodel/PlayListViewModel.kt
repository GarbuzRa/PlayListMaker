package com.example.playlistmaker.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.data.db.playlist.TrackInPlaylistEntity
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.PlaylistState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PlayListViewModel(private val interactor: PlaylistInteractor):ViewModel() {
    private val _playlistsState = MutableLiveData<PlaylistState>(PlaylistState.Load)
    val playlistState: MutableLiveData<PlaylistState> = _playlistsState

    private fun setState(playlistState: PlaylistState) {
        _playlistsState.postValue(playlistState)
    }

    fun getPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            interactor
                .getAllPlaylists()
                .collect { playlists ->
                    val playlistsWithParsedUri = playlists.map { playlist ->
                        playlist.copy(
                            imageUri = Uri.parse(playlist.imageUri).toString()
                        )
                    }
                    if (playlistsWithParsedUri.isEmpty()) {
                        setState(PlaylistState.Empty)
                    } else {
                        setState(PlaylistState.ShowPlaylists(playlistsWithParsedUri))
                    }
                }
        }
    }

   /* fun getTrackInPlaylist(playList: PlayList, track: Track)  {
        viewModelScope.launch {
            try {
                interactor.insertTrackToPlaylist(playList, track)
            } catch (e: Exception) {
                Log.e("PlayListViewModel", "Не удалось добавить трек в плейлист")
            }

        }
    } */
}