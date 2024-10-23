package com.example.playlistmaker.presentation.ui.mediateka.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistItemBinding
import com.example.playlistmaker.domain.model.PlayList

class PlaylistsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val binding = PlaylistItemBinding.bind(itemView)

    fun interface ClickListener {
        fun onClick(playList: PlayList)
    }

    fun bind(playList: PlayList, clickListener: ClickListener) {
        binding.namePlaylist.text = playList.name
        binding.descriptionPlaylist.text = setPluralsTracks(itemView.context, playList.trackCount)

        itemView.setOnClickListener{
            clickListener.onClick(playList)
        }

        Glide.with(itemView)
            .load(playList.imageTitle)
            .placeholder(R.drawable.placeholder)
            .into(binding.coverPlaylist)
    }

    fun setPluralsTracks(context: Context, count: Int): String {
        val trackPlural = context.resources.getQuantityString(R.plurals.count_tracks, count)
        return "${count} ${trackPlural}"
    }
}