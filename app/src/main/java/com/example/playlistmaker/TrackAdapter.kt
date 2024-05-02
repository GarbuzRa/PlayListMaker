package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView



class TrackAdapter(var trackList: MutableList<Track>, var clickListener: (Track) -> Unit) :
    RecyclerView.Adapter<TrackViewHolder>() {
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
        holder.itemView.setOnClickListener {
            clickListener(trackList[position])
        }
    }

    override fun getItemCount() = trackList.size

}