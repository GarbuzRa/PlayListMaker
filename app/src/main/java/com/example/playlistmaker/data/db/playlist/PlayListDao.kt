package com.example.playlistmaker.data.db.playlist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlayListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayList(playListEntity: PlayListEntity)

    @Delete
    suspend fun deletePlayList(playListEntity: PlayListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackToPlaylist(trackInPlaylistEntity: TrackInPlaylistEntity)

    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylists(): List<PlayListEntity>

    @Query("SELECT * FROM PLAYLISTS WHERE id = :id")
    suspend fun getPlaylistById(id: Int): PlayListEntity

    @Update
    suspend fun updatePlaylist(playListEntity: PlayListEntity)
}