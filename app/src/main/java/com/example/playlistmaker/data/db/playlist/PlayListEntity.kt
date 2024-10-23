package com.example.playlistmaker.data.db.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "playlists")
data class PlayListEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val imageTitle: String,
    val tracksId: ArrayList<Long>,
    val trackCount: Int,
    val imageUri: String?
    )
