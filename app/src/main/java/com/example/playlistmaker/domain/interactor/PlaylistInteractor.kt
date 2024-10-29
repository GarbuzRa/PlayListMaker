package com.example.playlistmaker.domain.interactor

import android.content.Context
import android.net.Uri
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlayListRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractor(val playListRepository: PlayListRepository) {

    suspend fun insertPlayList(playList: PlayList) {
        playListRepository.insertPlayList(playList)
    }

    suspend fun deletePlayList(playList: PlayList) {
        playListRepository.deletePlayList(playList)
    }

    suspend fun insertTrackToPlaylist(playList: PlayList, track: Track) {
        playListRepository.insertTrackToPlaylist(playList, track)
    }

    suspend fun getAllPlaylists(): Flow<List<PlayList>> {
       return playListRepository.getAllPlaylists()
    }

    suspend fun getPlaylistById(id: Int): PlayList {
        return playListRepository.getPlaylistById(id)
    }

    suspend fun saveImageToStorage(context: Context, uri: Uri): Uri? {
        return playListRepository.saveImageToStorage(context, uri)
    }
}