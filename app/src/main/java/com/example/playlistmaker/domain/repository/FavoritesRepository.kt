package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun insertFavorite(track : Track)
    suspend fun deleteFavorite(track: Track)
    fun getFavorites() : Flow<List<Track>>
    fun getFavoritesIds() : Flow<List<String>>
}