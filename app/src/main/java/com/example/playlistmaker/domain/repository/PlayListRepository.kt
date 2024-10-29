package com.example.playlistmaker.domain.repository

import android.content.Context
import android.net.Uri
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlayListRepository {
    suspend fun insertPlayList(playList: PlayList)

    suspend fun deletePlayList(playList: PlayList)

    suspend fun insertTrackToPlaylist(playList: PlayList, track: Track)

    suspend fun getAllPlaylists(): Flow<List<PlayList>>

    suspend fun getPlaylistById(id: Int): PlayList

    suspend fun saveImageToStorage(context: Context, uri: Uri): Uri?
}