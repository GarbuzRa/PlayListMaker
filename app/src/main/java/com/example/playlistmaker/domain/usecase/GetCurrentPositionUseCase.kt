package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.PlayerRepository

class GetCurrentPositionUseCase(private val playerRepository: PlayerRepository) {
    operator fun invoke(): Int {
        return playerRepository.getCurrentPosition()
    }
}