package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchTracksUseCase(private val trackRepository: TrackRepository) {
    fun execute(query: String, callback: (Result<List<Track>>) -> Unit) {
        trackRepository.searchTracks(query, callback)
    }
}


