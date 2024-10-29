package com.example.playlistmaker.presentation.ui.player

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistInPlayerItemBinding
import com.example.playlistmaker.domain.model.PlayList


class PlaylistInPlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = PlaylistInPlayerItemBinding.bind(view)

    fun bind(model: PlayList, clickListener: ClickListener) {
        binding.bottomSheetNamePlaylist.text = model.name
        binding.bottomSheetDecsPlaylist.text = setTracksAmount(itemView.context, model.trackCount)
        val previewUri = model.imageUri.let { Uri.parse(it) }

        Glide.with(itemView)
            .load(previewUri)
            .placeholder(R.drawable.placeholder)
            .into(binding.bottomSheetCoverPlaylist)
        itemView.setOnClickListener {
            clickListener.onClick(model)
        }
    }

    private fun setTracksAmount(context: Context, count: Int): String {
        val track = context.resources.getQuantityString(R.plurals.count_tracks, count)
        return "$count $track"
    }

    fun interface ClickListener {
        fun onClick(playlist: PlayList)
    }
    }