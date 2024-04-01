package com.example.playlistmaker

import android.view.RoundedCorner
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat

class TrackViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
    val trackImage  : ImageView = itemView.findViewById(R.id.track_image)
    val trackName   : TextView = itemView.findViewById(R.id.track_name)
    val trackArtist : TextView = itemView.findViewById(R.id.track_artist)
    val trackTime   : TextView = itemView.findViewById(R.id.track_time)

    fun bind (track: Track) {
        Glide.with(trackImage)
            .load(track.artworkUrl100)
            .centerInside()
            .transform(RoundedCorners(4))
            .placeholder(R.drawable.placeholder)
            .into(trackImage)

        trackName.text = track.trackName
        trackArtist.text = track.artistName
        trackTime.text = track.trackTime

        itemView.setOnClickListener{
            Toast.makeText(itemView.context, "Я УБЬЮ РЕБЕНКА", Toast.LENGTH_SHORT).show()
        }

    }
}