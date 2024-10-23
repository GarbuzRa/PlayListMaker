package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.interactor.FavoritesInteractor
import kotlinx.coroutines.launch

class FavoritesViewModel(private val interactor: FavoritesInteractor): ViewModel() {

    val _favoriteTracks = MutableLiveData<List<Track>>()
    val favoriteTracks : LiveData<List<Track>> = _favoriteTracks
    fun loadFavoriteTracks(){
        viewModelScope.launch {
            interactor.getFavorites().collect{ tracks ->
                _favoriteTracks.postValue(tracks)
            }
        }
    }
}