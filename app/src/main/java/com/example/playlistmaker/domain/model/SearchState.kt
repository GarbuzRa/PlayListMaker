package com.example.playlistmaker.domain.model

sealed class SearchState {
    object ShowHistory : SearchState()
    object Loading : SearchState()
    data class ShowSearchResults(val tracks: List<Track>) : SearchState()
    object Error : SearchState()
    object Empty : SearchState()
    object NoHistory : SearchState()
}