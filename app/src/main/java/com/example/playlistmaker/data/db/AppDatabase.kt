package com.example.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoritesEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}


