package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class GetSearchHistoryUseCase(private val trackRepository: TrackRepository) {
    fun execute(): List<Track> {
        return trackRepository.getSearchHistory()
    }
}