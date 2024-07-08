package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchViewModel(private val trackRepository: TrackRepository) : ViewModel() {
    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _historyTracks = MutableLiveData<List<Track>>()
    val historyTracks: LiveData<List<Track>> = _historyTracks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _showError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean> = _showError

    private val _showEmpty = MutableLiveData<Boolean>()
    val showEmpty: LiveData<Boolean> = _showEmpty

    fun searchTracks(query: String) {
        _isLoading.value = true
        _showError.value = false
        _showEmpty.value = false

        trackRepository.searchTracks(query) { result ->
            _isLoading.value = false
            result.fold(
                onSuccess = { tracks ->
                    if (tracks.isNotEmpty()) {
                        _tracks.value = tracks
                    } else {
                        _showEmpty.value = true
                    }
                },
                onFailure = {
                    _showError.value = true
                }
            )
        }
    }

    fun getSearchHistory() {
        _historyTracks.value = trackRepository.getSearchHistory()
    }

    fun addToSearchHistory(track: Track) {
        trackRepository.addToSearchHistory(track)
        getSearchHistory()
    }

    fun clearSearchHistory() {
        trackRepository.clearSearchHistory()
        getSearchHistory()
    }
}