package com.example.playlistmaker

import com.example.playlistmaker.domain.model.Track

data class SearchResponse(
    val resultCount: Int,
    val results: MutableList<Track>
)
