package com.example.playlistmaker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.data.db.playlist.TrackInPlaylistEntity
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PlayListViewModel(private val interactor: PlaylistInteractor):ViewModel() {
   private val _playLists = MutableLiveData<List<PlayList>>()
    val playLists: LiveData<List<PlayList>> = _playLists


    fun getPlayLists() {
        viewModelScope.async {
            interactor.getAllPlaylists().collect { playList ->
                _playLists.postValue(playList)
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