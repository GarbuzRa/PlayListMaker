package com.example.playlistmaker.presentation.ui.mediateka.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.presentation.ui.mediateka.adapter.PlaylistsViewHolder.ClickListener

class PlaylistsAdapter(val clickListener: ClickListener): RecyclerView.Adapter<PlaylistsViewHolder>() {
    val playlists = ArrayList<PlayList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return PlaylistsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
        holder.bind(playlists[position], clickListener)
    }
}