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
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()

        val trackJson = intent.getStringExtra(CURRENT_TRACK)
        val track = Gson().fromJson(trackJson, Track::class.java)
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
        binding.likeButton.setOnClickListener{viewModel.onFavoriteClicked()}

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

            Glide.with(this)
                .load(trackUiState.artworkUrl)
                .placeholder(R.drawable.placeholder)
                .transform(RoundedCorners(15))
                .into(binding.coverImageView)
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            binding.playImageView.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
        }

        viewModel.currentPosition.observe(this) { position ->
            binding.trackTimeTextView.text = position
        }
        viewModel.isFavorite.observe(this){isFavorite ->
            binding.likeButton.setImageResource(
                if (isFavorite) R.drawable.like_active
                else R.drawable.like
            )
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    companion object {
        const val CURRENT_TRACK = "current_track"
    }
}