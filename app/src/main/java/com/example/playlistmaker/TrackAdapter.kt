package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.concurrent.timerTask

class TrackAdapter() : RecyclerView.Adapter<TrackViewHolder>() {
    private var trackList: MutableList<Track> = mutableListOf()
    fun updateList(trackList: MutableList<Track>) {
        this.trackList = trackList
        notifyDataSetChanged()
    }
    fun clearList() {
        trackList.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)

        return TrackViewHolder(view)
    }
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position])
    }

    override fun getItemCount() = trackList.size

}