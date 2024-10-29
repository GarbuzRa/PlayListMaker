package com.example.playlistmaker.presentation.ui.player

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.ui.playlist.NewPlaylistFragment
import com.example.playlistmaker.presentation.viewmodel.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerActivity : AppCompatActivity(), PlaylistInPlayerViewHolder.ClickListener {
    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModel()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var adapter: PlaylistInPlayerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupBottomSheet()
        observeViewModel()
        setupPlaylistsRecyclerView()

        val trackJson = intent.getStringExtra(CURRENT_TRACK)
        val track = Gson().fromJson(trackJson, Track::class.java)
        viewModel.preparePlayer(track.trackId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            viewModel.refreshPlaylists()
        }
    }


    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetAudioPlayer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.bottomSheetOverlay.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.bottomSheetOverlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.bottomSheetOverlay.alpha = slideOffset
            }
        })

        binding.bottomSheetAddPlaylistBtn.setOnClickListener {
            showNewPlaylistFragment()
        }

        binding.bottomSheetOverlay.setOnClickListener {
            setBottomSheetVis(false)
        }

        binding.bottomSheetOverlay.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                setBottomSheetVis(false)
            }
        }
    }

    private fun showNewPlaylistFragment() {
        val newPlaylistFragment = NewPlaylistFragment()
        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, newPlaylistFragment)
            .addToBackStack(null)
            .commit()
        setBottomSheetVis(false)
    }

    private fun setupPlaylistsRecyclerView() {
        adapter = PlaylistInPlayerAdapter(this)
        binding.bottomSheetRecyclerView.adapter = adapter
        binding.bottomSheetRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.playlists.observe(this) { playlists ->
            adapter.playlists = ArrayList(playlists)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.playImageView.setOnClickListener { viewModel.playbackControl() }
        binding.addImageView.setOnClickListener {
            setBottomSheetVis(true)
            viewModel.refreshPlaylists()
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

    private fun setBottomSheetVis(visible: Boolean) {
        bottomSheetBehavior.state = if (visible) {
            BottomSheetBehavior.STATE_EXPANDED
        } else {
            BottomSheetBehavior.STATE_HIDDEN
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

    override fun onClick(playlist: PlayList) {
        if (!viewModel.isInPlaylist(
                playlist = playlist,
                trackId = viewModel.currentTrack.value?.trackId?.toLong() ?: 0
            )
        ) {
            viewModel.onAddToPlaylistClick(playlist = playlist, track = viewModel.currentTrack.value!!)
            Toast.makeText(
                this.applicationContext,
                "${getString(R.string.added_to_playlist)} ${playlist.name}",
                Toast.LENGTH_SHORT
            )
                .show()
            playlist.trackCount = playlist.tracksId.size
            BottomSheetBehavior.from(binding.bottomSheetAudioPlayer).apply {
                state = BottomSheetBehavior.STATE_HIDDEN
            }
        } else {
            Toast.makeText(
                this.applicationContext,
                "${getString(R.string.track_is_already_in_playlist)} ${playlist.name}",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        adapter.notifyDataSetChanged()
    }
}