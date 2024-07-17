package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.TrackRepository

class ClearSearchHistoryUseCase(private val trackRepository: TrackRepository) {
    fun execute() {
        trackRepository.clearSearchHistory()
    }
}