// presentation/ui/player/PlayerActivity.kt
package com.example.playlistmaker.presentation.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.viewmodel.PlayerViewModel
import com.example.playlistmaker.presentation.viewmodel.PlayerViewModelFactory
import com.example.playlistmaker.util.Creator
import com.google.gson.Gson

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var viewModel: PlayerViewModel
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, PlayerViewModelFactory()).get(PlayerViewModel::class.java)

        handler = Handler(Looper.getMainLooper())

        val trackJson = intent.getStringExtra(CURRENT_TRACK)
        val track = Gson().fromJson(trackJson, Track::class.java)

        setupListeners()
        observeViewModel()

        viewModel.preparePlayer(track.trackId)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.playImageView.setOnClickListener { viewModel.playbackControl() }
        binding.likeButton.setOnClickListener {
            showMessage(getString(R.string.added_to_liked, viewModel.trackData.value?.trackName))
        }
        binding.addImageView.setOnClickListener {
            showMessage(getString(R.string.added_to_library, viewModel.trackData.value?.trackName))
        }
    }

    private fun observeViewModel() {
        viewModel.trackData.observe(this) { trackUiState ->
            binding.trackNameTextView.text = trackUiState.trackName
            binding.trackArtistTextView.text = trackUiState.artistName
            binding.trackGenreTextView.text = trackUiState.primaryGenreName
            binding.trackAlbumTextView.text = trackUiState.collectionName
            binding.trackCountryTextView.text = trackUiState.country
            binding.trackLengthTextView.text = trackUiState.trackTimeMillis
            binding.trackYearTextView.text = trackUiState.releaseDate

            Creator.loadImage(
                this,
                trackUiState.artworkUrl,
                binding.coverImageView,
                R.drawable.placeholder,
                15
            )
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            binding.playImageView.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
            if (isPlaying) {
                startPlayback()
            } else {
                pausePlayback()
            }
        }

        viewModel.currentPosition.observe(this) { position ->
            binding.trackTimeTextView.text = position
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startPlayback() {
        handler.post(updateTimeTask)
    }

    private fun pausePlayback() {
        handler.removeCallbacks(updateTimeTask)
    }

    private val updateTimeTask = object : Runnable {
        override fun run() {
            viewModel.updateCurrentPosition(Creator.providePlayerRepository().getCurrentPosition())
            handler.postDelayed(this, 500L)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Creator.providePlayerRepository().releasePlayer()
        handler.removeCallbacks(updateTimeTask)
    }

    companion object {
        const val CURRENT_TRACK = "current_track"
    }
}