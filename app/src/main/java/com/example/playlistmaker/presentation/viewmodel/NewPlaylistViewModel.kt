package com.example.playlistmaker.presentation.viewmodel

import android.app.ActivityManager.TaskDescription
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.PlayList
import kotlinx.coroutines.launch
import java.net.URI
import java.util.UUID

open class NewPlaylistViewModel(val interactor: PlaylistInteractor): ViewModel() {
    val _savedCoverUri = MutableLiveData<Uri?>() //Следит за ссылкой на картинку
    val savedCoverUri: LiveData<Uri?> = _savedCoverUri

    fun createPlaylist(name: String, description: String, coverUri: Uri?) {
        val playList = PlayList(
            id = 0,
            name = name,
            description = description,
            imageTitle = "playlist_${UUID.randomUUID()}.jpg",
            tracksId = arrayListOf<Long>(),
            trackCount = 0,
            imageUri = coverUri?.toString()?:""
        )
        viewModelScope.launch {
            interactor.insertPlayList(playList)
        }
    }

    fun saveImageToStorage(context: Context, uri: Uri){
        viewModelScope.launch {
            val savedUri = interactor.saveImageToStorage(context,uri)
            _savedCoverUri.postValue(savedUri)
        }

    }
}