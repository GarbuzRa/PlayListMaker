package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlayerRepository

class PrepareTrackUseCase(val playerRepository: PlayerRepository) {
    operator fun invoke(track: Track) {
        playerRepository.preparePlayer(track)
    }
}