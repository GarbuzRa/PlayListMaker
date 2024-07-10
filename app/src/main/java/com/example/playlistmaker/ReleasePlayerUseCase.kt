package com.example.playlistmaker


import com.example.playlistmaker.domain.repository.PlayerRepository

class ReleasePlayerUseCase(private val playerRepository: PlayerRepository) {
    operator fun invoke() {
        playerRepository.releasePlayer()
    }
}