package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.PlayerRepository

class PlayTrackUseCase(val playerRepository: PlayerRepository) {
    operator fun invoke() {
        playerRepository.playTrack()
    }
}