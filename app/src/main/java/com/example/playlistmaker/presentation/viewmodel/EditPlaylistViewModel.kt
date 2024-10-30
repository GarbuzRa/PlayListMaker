package com.example.playlistmaker.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.PlayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EditPlaylistViewModel(private val playlistsInteractor: PlaylistInteractor) :
    NewPlaylistViewModel(playlistsInteractor) {

    private val _playlist = MutableLiveData<PlayList>()
    val playlist: LiveData<PlayList> get() = _playlist

    fun getPlaylist(playlist: PlayList) {
        _playlist.postValue(playlist)
    }

    fun getCover() {
        viewModelScope.launch(Dispatchers.IO) {
            playlistsInteractor
                .getAllPlaylists()
                .collect { playlists ->
                    playlists.map { playlist ->
                        playlist.copy(
                            imageUri = Uri.parse(playlist.imageUri).toString()
                        )
                    }
                }
        }
    }

    fun modifyData(name: String, description: String, cover: String, coverUri: Uri?, originalPlayList: PlayList) {
        viewModelScope.launch {
            playlistsInteractor.modifyData(name, description, cover, coverUri, originalPlayList)
        }
    }
}