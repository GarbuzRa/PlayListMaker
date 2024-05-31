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
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var track: Track
    private lateinit var playImageView: ImageView
    private lateinit var trackTimeTextView: TextView
    private lateinit var handler: Handler

    private fun getTrack(json: String?) = Gson().fromJson(json, Track::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        val playerBackButton = findViewById<Button>(R.id.back_button)
        val addImageView = findViewById<ImageView>(R.id.add_image_view)
        playImageView = findViewById<ImageView>(R.id.play_image_view)
        val likeButton = findViewById<ImageView>(R.id.like_button)
        val coverImageView = findViewById<ImageView>(R.id.cover_image_view)
        val trackNameTextView = findViewById<TextView>(R.id.track_name_text_view)
        val trackArtistTextView = findViewById<TextView>(R.id.track_artist_text_view)
        val trackLengthTextView = findViewById<TextView>(R.id.track_length_text_view)
        val trackGenreTextView = findViewById<TextView>(R.id.track_genre_text_view)
        trackTimeTextView = findViewById<TextView>(R.id.track_time_text_view)
        val trackCountryTextView = findViewById<TextView>(R.id.track_country_text_view)
        val trackYearTextView = findViewById<TextView>(R.id.track_year_text_view)
        val trackAlbumTextView = findViewById<TextView>(R.id.track_album_text_view)
        handler = Handler(Looper.getMainLooper())

        track = getTrack(intent.getStringExtra(CURRENT_TRACK))
        trackNameTextView.text = track.trackName
        trackArtistTextView.text = track.artistName
        trackGenreTextView.text = track.primaryGenreName
        trackAlbumTextView.text = track.collectionName
        trackCountryTextView.text = track.country
        trackTimeTextView.text = "00:00"
        trackLengthTextView.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        trackYearTextView.text = LocalDateTime.parse(
            track.releaseDate,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        ).year.toString()

        Glide.with(coverImageView)
            .load(track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(15))
            .into(coverImageView)

        playerBackButton.setOnClickListener {
            finish()
        }

        playImageView.setOnClickListener {
            togglePlayback()
        }

        likeButton.setOnClickListener {
            showMessage(getString(R.string.added_to_liked, track.trackName))
        }

        addImageView.setOnClickListener {
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
        playImageView.setImageResource(R.drawable.play)
        trackTimeTextView.text = "00:00"
        handler.removeCallbacks(updateTimeTask)
    }
  private val updateTimeTask = object: Runnable {
      override fun run() {
          mediaPlayer?.let {
               val currentPosition = it.currentPosition
              trackTimeTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentPosition)
              //у текстВью (трек таймер) меняем текст. после ровно мы форматируем через класс SimpleDateFormat
              // (задаем формат (ммсс) ||| Locale.getDefault() (берем фу-ию локализованного времени) |||
              // format(currentPosition) - результат форматируем из мс
          }
      }
  }
    private fun startPlayback() {
        mediaPlayer?.start()
        playImageView.setImageResource(R.drawable.pause)
        handler.post(updateTimeTask)
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        playImageView.setImageResource(R.drawable.play)
        handler.removeCallbacks(updateTimeTask)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateTimeTask)
    }

}