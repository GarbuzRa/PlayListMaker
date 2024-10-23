package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.favorites.AppDatabase
import com.example.playlistmaker.data.db.favorites.FavoritesEntity
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavoritesRepositoryImpl(val appDataBase: AppDatabase) : FavoritesRepository {
    override suspend fun insertFavorite(track : Track){
        GlobalScope.launch {
            appDataBase.favoritesDao().insertTrack(track.toFavoritesEntity())
        }
    }
    override suspend fun deleteFavorite(track: Track){
        GlobalScope.launch {
            appDataBase.favoritesDao().deleteTracK(track.toFavoritesEntity())
        }
    }
    override fun getFavorites() : Flow<List<Track>> {
        return appDataBase.favoritesDao().getFavoriteTracks().map { list ->
            list.map { it.toTrack() }
        }.map { it.reversed() }
    }
    override fun getFavoritesIds() : Flow<List<String>> {
        return appDataBase.favoritesDao().getFavoriteTracksIds()
    }

    fun Track.toFavoritesEntity() : FavoritesEntity {
        return FavoritesEntity(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl
        )
    }

    fun FavoritesEntity.toTrack() : Track {
        return Track(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl,
            isFavorite = true
        )
    }
}