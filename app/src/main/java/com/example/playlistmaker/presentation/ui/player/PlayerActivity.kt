// presentation/ui/player/PlayerActivity.kt
package com.example.playlistmaker.presentation.ui.player

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.ui.mediateka.adapter.PlaylistsAdapter
import com.example.playlistmaker.presentation.ui.mediateka.adapter.PlaylistsViewHolder
import com.example.playlistmaker.presentation.ui.playlist.NewPlaylistFragment
import com.example.playlistmaker.presentation.viewmodel.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.jvm.internal.Ref.BooleanRef

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModel()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

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

        binding.bottomSheetAddPlaylistBtn.setOnClickListener {
            showNewPlaylistFragment()
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
        val adapter = PlaylistsAdapter(object : PlaylistsViewHolder.ClickListener {
            override fun onClick(playlist: PlayList) {
                viewModel.addTrackToPlaylist(playlist)
                setBottomSheetVis(false)
            }
        })
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

    private fun setBottomSheetVis(visible: Boolean){
        if (visible){
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetOverlay.visibility = View.VISIBLE
        } else{
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            binding.bottomSheetOverlay.visibility = View.GONE
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