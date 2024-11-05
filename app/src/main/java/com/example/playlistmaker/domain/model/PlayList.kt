package com.example.playlistmaker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PlayList(
    val id: Int,
    val name: String,
    val description: String,
    val imageTitle: String,
    val tracksId: ArrayList<Long>,
    var trackCount: Int,
    val imageUri: String?
) : Parcelable
