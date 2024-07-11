package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.PlayerRepository

class SetOnCompletionListenerUseCase(private val playerRepository: PlayerRepository) {
    operator fun invoke(listener: () -> Unit) {
        playerRepository.setOnCompletionListener(listener)
    }
}