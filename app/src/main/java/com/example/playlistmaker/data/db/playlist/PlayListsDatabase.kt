package com.example.playlistmaker.data.db.playlist

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlayListEntity::class, TrackInPlaylistEntity::class], version = 1)
 abstract class PlayListsDatabase: RoomDatabase() {
  abstract fun playlistDao(): PlayListDao
}