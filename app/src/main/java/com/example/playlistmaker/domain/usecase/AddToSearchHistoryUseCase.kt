package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class AddToSearchHistoryUseCase(private val trackRepository: TrackRepository) {
    fun execute(track: Track) {
        trackRepository.addToSearchHistory(track)
    }
}