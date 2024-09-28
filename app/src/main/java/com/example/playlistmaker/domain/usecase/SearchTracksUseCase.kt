package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow

class SearchTracksUseCase(private val trackRepository: TrackRepository) {
    fun execute(query: String): Flow<Result<List<Track>>> {
       return trackRepository.searchTracks(query)
    }
}


