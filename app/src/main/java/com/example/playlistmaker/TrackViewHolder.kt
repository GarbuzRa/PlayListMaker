package com.example.playlistmaker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
    val trackImage  : ImageView = itemView.findViewById(R.id.track_image)
    val trackName   : TextView = itemView.findViewById(R.id.track_name)
    val trackArtist : TextView = itemView.findViewById(R.id.track_artist)
    val trackTime   : TextView = itemView.findViewById(R.id.track_time)

    fun bind (track: Track) {
       if (track.artworkUrl100.isNotEmpty()) {
           Glide.with(trackImage)
            .load(track.artworkUrl100)
            .centerInside()
            .transform(RoundedCorners(4))
            .placeholder(R.drawable.placeholder)
            .into(trackImage)
       } else {
           trackImage.setImageResource(R.drawable.placeholder)
       }

        trackName.text = track.trackName
        trackArtist.text = track.artistName
        trackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        itemView.setOnClickListener{
            Toast.makeText(itemView.context, "Я НЕ УБЬЮ РЕБЕНКА", Toast.LENGTH_SHORT).show()
        }

    }
}