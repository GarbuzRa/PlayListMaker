package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.usecase.AddToSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.ClearSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.SearchTracksUseCase
import com.example.playlistmaker.util.Creator

class SearchViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            val trackRepository = Creator.provideTrackRepository()
            return SearchViewModel(
                SearchTracksUseCase(trackRepository),
                GetSearchHistoryUseCase(trackRepository),
                AddToSearchHistoryUseCase(trackRepository),
                ClearSearchHistoryUseCase(trackRepository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}