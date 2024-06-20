package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
     private lateinit var binding: ActivityPlayerBinding
    private lateinit var track: Track
    private lateinit var handler: Handler

    private fun getTrack(json: String?) = Gson().fromJson(json, Track::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        handler = Handler(Looper.getMainLooper())

        track = getTrack(intent.getStringExtra(CURRENT_TRACK))
        binding.trackNameTextView.text = track.trackName
        binding.trackArtistTextView.text = track.artistName
        binding.trackGenreTextView.text = track.primaryGenreName
        binding.trackAlbumTextView.text = track.collectionName
        binding.trackCountryTextView.text = track.country
        binding.trackTimeTextView.text = "00:00"
        binding.trackLengthTextView.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
       binding.trackYearTextView.text = LocalDateTime.parse(
            track.releaseDate,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        ).year.toString()

        Glide.with(binding.coverImageView)
            .load(track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(15))
            .into(binding.coverImageView)

       binding.backButton.setOnClickListener {
            finish()
        }

        binding.playImageView.setOnClickListener {
            togglePlayback()
        }

        binding.likeButton.setOnClickListener {
            showMessage(getString(R.string.added_to_liked, track.trackName))
        }

        binding.addImageView.setOnClickListener {
            showMessage(getString(R.string.added_to_library, track.trackName))
        }
    }

    private fun showMessage(message : String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
     private fun togglePlayback() { //функция переключения состояния плеера
        if (mediaPlayer == null) {
            initializeMediaPlayer()
        }
         if (mediaPlayer?.isPlaying == true) {
             pausePlayback()
         } else {
             startPlayback()
         }
     }

     private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(track.previewUrl)
            prepare()
            setOnCompletionListener {onPlaybackCompleted()}
        }
    }

    private fun onPlaybackCompleted() {
        binding.playImageView.setImageResource(R.drawable.play)
        binding.trackTimeTextView.text = "00:00"
        handler.removeCallbacks(updateTimeTask)
    }
  private val updateTimeTask = object: Runnable {
      override fun run() {
          mediaPlayer?.let {
               val currentPosition = it.currentPosition
              binding.trackTimeTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentPosition)
              handler.postDelayed(this, 500L)
              //у текстВью (трек таймер) меняем текст. после ровно мы форматируем через класс SimpleDateFormat
              // (задаем формат (ммсс) ||| Locale.getDefault() (берем фу-ию локализованного времени) |||
              // format(currentPosition) - результат форматируем из мс
          }
      }
  }
    private fun startPlayback() {
        mediaPlayer?.start()
        binding.playImageView.setImageResource(R.drawable.pause)
        handler.post(updateTimeTask)
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        binding.playImageView.setImageResource(R.drawable.play)
        handler.removeCallbacks(updateTimeTask)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateTimeTask)
    }

}