package com.example.playlistmaker.presentation.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.usecase.GetTrackUseCase
import com.example.playlistmaker.domain.usecase.PlayTrackUseCase
import com.example.playlistmaker.domain.usecase.PauseTrackUseCase
import com.example.playlistmaker.util.Creator
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var track: Track
    private lateinit var handler: Handler
    private val getTrackUseCase: GetTrackUseCase = Creator.provideGetTrackUseCase()
    private val playTrackUseCase: PlayTrackUseCase = Creator.providePlayTrackUseCase()
    private val pauseTrackUseCase: PauseTrackUseCase = Creator.providePauseTrackUseCase()
    private val playerRepository = Creator.providePlayerRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        val trackJson = intent.getStringExtra(CURRENT_TRACK)
        track = Gson().fromJson(trackJson, Track::class.java)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.trackNameTextView.text = track.trackName
        binding.trackArtistTextView.text = track.artistName
        binding.trackGenreTextView.text = track.primaryGenreName
        binding.trackAlbumTextView.text = track.collectionName
        binding.trackCountryTextView.text = track.country
        binding.trackTimeTextView.text = "00:00"
        binding.trackLengthTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
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

        playerRepository.preparePlayer(track)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.playImageView.setOnClickListener { togglePlayback() }
        binding.likeButton.setOnClickListener {
            showMessage(getString(R.string.added_to_liked, track.trackName))
        }
        binding.addImageView.setOnClickListener {
            showMessage(getString(R.string.added_to_library, track.trackName))
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun togglePlayback() {
        if (playerRepository.isPlaying()) {
            pauseTrackUseCase()
            pausePlayback()
        } else {
            playTrackUseCase()
            startPlayback()
        }
    }

    private fun startPlayback() {
        binding.playImageView.setImageResource(R.drawable.pause)
        handler.post(updateTimeTask)
    }

    private fun pausePlayback() {
        binding.playImageView.setImageResource(R.drawable.play)
        handler.removeCallbacks(updateTimeTask)
    }

    private val updateTimeTask = object : Runnable {
        override fun run() {
            val currentPosition = playerRepository.getCurrentPosition()
            binding.trackTimeTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentPosition)
            handler.postDelayed(this, 500L)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerRepository.releasePlayer()
        handler.removeCallbacks(updateTimeTask)
    }

    companion object {
        const val CURRENT_TRACK = "current_track"
    }
}