package com.example.playlistmaker.data.db.playlist

import androidx.room.TypeConverter

class TracksIdsConverter {
    @TypeConverter
    fun fromList(list: ArrayList<Long>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String): ArrayList<Long> {
        if (data.isEmpty()) return ArrayList()
        return ArrayList(
            data.split(",")
                .mapNotNull { it.trim().toLongOrNull() }
        )
    }
}