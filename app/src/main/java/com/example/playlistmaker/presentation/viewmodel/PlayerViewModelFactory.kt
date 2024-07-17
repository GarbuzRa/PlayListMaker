package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.util.Creator

class PlayerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(
                Creator.provideGetTrackUseCase(),
                Creator.providePlayTrackUseCase(),
                Creator.providePauseTrackUseCase(),
                Creator.providePrepareTrackUseCase(),
                Creator.provideReleasePlayerUseCase(),
                Creator.provideGetCurrentPositionUseCase(),
                Creator.provideSetOnCompletionListenerUseCase()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}