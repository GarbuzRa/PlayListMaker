package com.example.playlistmaker.data.db.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "track_in_playlist")
data class TrackInPlaylistEntity(
    @PrimaryKey
    val trackId : String,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Int,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val previewUrl : String,
    val timeInsert: Long
)
