package com.example.playlistmaker.data.db.playlist

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PlayListEntity::class, TrackInPlaylistEntity::class], version = 3)
@TypeConverters(TracksIdsConverter::class)
 abstract class PlayListsDatabase: RoomDatabase() {
  abstract fun playlistDao(): PlayListDao
}