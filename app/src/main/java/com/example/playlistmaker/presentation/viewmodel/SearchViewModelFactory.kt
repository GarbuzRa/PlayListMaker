package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.util.Creator

class SearchViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(Creator.provideTrackRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}