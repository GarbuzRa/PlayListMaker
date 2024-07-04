package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.PlayerRepository

class PauseTrackUseCase(val playerRepository: PlayerRepository) {
    operator fun invoke() {
        playerRepository.pauseTrack()
    }
}