package com.example.playlistmaker.presentation.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.PlayList

class PlaylistInPlayerAdapter(private val clickListener: PlaylistInPlayerViewHolder.ClickListener) :
    RecyclerView.Adapter<PlaylistInPlayerViewHolder>() {
    var playlists = ArrayList<PlayList>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistInPlayerViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.playlist_in_player_item, parent, false)
        return PlaylistInPlayerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun onBindViewHolder(holder: PlaylistInPlayerViewHolder, position: Int) {
        holder.bind(playlists[position], clickListener)
    }
}
