package com.example.playlistmaker.data.db.favorites

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoritesEntity::class], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}


