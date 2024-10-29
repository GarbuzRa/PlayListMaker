package com.example.playlistmaker.domain.model

sealed class PlaylistState {
    data class ShowPlaylists(val playlists: List<PlayList>): PlaylistState()
    object Empty : PlaylistState()
    object Load : PlaylistState()
}