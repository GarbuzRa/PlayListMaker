package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class FavoritesInteractor(val favoritesRepository: FavoritesRepository) {
    suspend fun insertFavorite(track : Track){
        favoritesRepository.insertFavorite(track)
    }
    suspend fun deleteFavorite(track: Track){
        favoritesRepository.deleteFavorite(track)
    }
    fun getFavorites() : Flow<List<Track>> {
        return favoritesRepository.getFavorites()
    }
    fun getFavoritesIds() : Flow<List<String>> {
        return favoritesRepository.getFavoritesIds()
    }
}



