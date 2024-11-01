package com.example.playlistmaker.presentation.ui.playlist

import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSelectedPlaylistBinding
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.ui.player.PlayerActivity
import com.example.playlistmaker.presentation.ui.search.TrackAdapter
import com.example.playlistmaker.presentation.viewmodel.SelectedPlaylistViewModel
import com.example.playlistmaker.util.CURRENT_TRACK
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class SelectedPlaylistFragment : Fragment() {
    private var _binding: FragmentSelectedPlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<SelectedPlaylistViewModel>()
    private val adapter = TrackAdapter(mutableListOf())
    private var currentPlaylistId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectedPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Извлечение ID плейлиста из аргументов
        currentPlaylistId = arguments?.getParcelable<PlayList>("selected_playlist")?.id ?: -1

        setupAdapters()
        setupBottomSheetBehaviors()
        setupObservers()
        setupClickListeners()

        // Загрузка плейлиста
        viewModel.loadPlaylist(currentPlaylistId)
    }

    private fun setupAdapters() {
        adapter.clickListener = { openAudioPlayer(it) }
        adapter.longClickListener = { showDialog(it.trackId?.toLong() ?: 0) }
        binding.rvSelectedPlaylist.adapter = adapter
    }

    private fun setupBottomSheetBehaviors() {
        val screenHeight = resources.displayMetrics.heightPixels
        val allowableHeight = (screenHeight * 0.25).toInt()

        val bottomSheetBehavior =
            BottomSheetBehavior.from(binding.bottomMenuSelectedPlaylist).apply {
                state = BottomSheetBehavior.STATE_HIDDEN
            }

        val bottomSheetBehaviorPlaylist =
            BottomSheetBehavior.from(binding.playlistBottomMenuTracks).apply {
                state = BottomSheetBehavior.STATE_COLLAPSED
                peekHeight = allowableHeight
            }

        setupBottomSheetCallbacks(bottomSheetBehavior, bottomSheetBehaviorPlaylist)
    }

    private fun setupBottomSheetCallbacks(
        bottomSheetBehavior: BottomSheetBehavior<LinearLayout>,
        bottomSheetBehaviorPlaylist: BottomSheetBehavior<LinearLayout>
    ) {
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottomSheetBehaviorPlaylist.isHideable = false
                        binding.menuBottomSheetOverlay.visibility = View.GONE
                    }
                    else -> {
                        binding.menuBottomSheetOverlay.visibility = View.VISIBLE
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.menuBottomSheetOverlay.alpha = slideOffset
            }
        })
    }

    private fun setupObservers() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            showSelectedPlaylist(playlist)
        }

        viewModel.playlistTracks.observe(viewLifecycleOwner) { tracks ->
            showContent(tracks)
            checkTracks(tracks.size)
        }

        viewModel.trackCount.observe(viewLifecycleOwner) { trackCount ->
            binding.trackAmountSelectedPlaylist.text = countTracks(trackCount)
        }

        viewModel.playlistTime.observe(viewLifecycleOwner) { time ->
            renderDuration(time)
        }
    }

    private fun setupClickListeners() {
        binding.toolbarSelectedPlaylist.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.editMenuSelectedPlaylist.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("modify_playlist", viewModel.playlist.value)
            }
            findNavController().navigate(
                R.id.action_selectedPlaylistFragment_to_modifyPlaylistFragment,
                bundle
            )
        }

        binding.shareSelectedPlaylist.setOnClickListener { toShare() }
        binding.shareMenuSelectedPlaylist.setOnClickListener {
            binding.bottomMenuSelectedPlaylist.visibility = View.GONE
            toShare()
        }

        binding.deleteMenuSelectedPlaylist.setOnClickListener {
            binding.bottomMenuSelectedPlaylist.visibility = View.GONE
            toDelete()
        }

        binding.menuDotsSelectedPlaylist.setOnClickListener {
            showBottomMenu()
        }

        binding.menuBottomSheetOverlay.setOnClickListener {
            binding.bottomMenuSelectedPlaylist.visibility = View.GONE
        }
    }

    private fun showBottomMenu() {
        val playlist = viewModel.playlist.value ?: return
        binding.bottomMenuSelectedPlaylist.visibility = View.VISIBLE
        binding.titleBottomSelectedPlaylist.text = playlist.name
        binding.trackAmountBottomSelectedPlaylist.text = countTracks(playlist.trackCount)
        binding.menuBottomSheetOverlay.visibility = View.VISIBLE

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomMenuSelectedPlaylist)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun checkTracks(trackCount: Int) {
        binding.noTracksInPlaylist.visibility =
            if (trackCount == 0) View.VISIBLE else View.GONE
    }

    private fun renderDuration(time: Long) {
        val totalMinutes = (time / (1000 * 60)).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val formattedDuration = when {
            hours > 0 -> {
                val hoursString = resources.getQuantityString(R.plurals.hours, hours, hours)
                val minutesString = resources.getQuantityString(R.plurals.minutes, minutes, minutes)
                "$hoursString $minutesString"
            }
            else -> {
                resources.getQuantityString(R.plurals.minutes, totalMinutes, totalMinutes)
            }
        }
        binding.timeSelectedPlaylist.text = formattedDuration
    }

    private fun toDelete() {
        MaterialAlertDialogBuilder(requireContext(), R.style.dialog)
            .setMessage(getString(R.string.isDeletePlaylist))
            .setNegativeButton(getString(R.string.No)) { _, _ -> }
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                viewModel.deletePlaylist()
                findNavController().navigateUp()
            }.show()
    }

    private fun toShare() {
        val tracks = viewModel.playlistTracks.value ?: emptyList()
        if (tracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.noPlaylistToShare),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val message = formMessage(tracks)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    }

    private fun formMessage(tracks: List<Track>): String {
        val playlist = viewModel.playlist.value ?: return ""
        val name = playlist.name
        val descript = playlist.description
        val stringBuilder = StringBuilder()
        with(stringBuilder) {
            append("$name\n")
            append("$descript\n")
            append("${countTracks(tracks.size)}\n")
        }
        for ((index, track) in tracks.withIndex()) {
            stringBuilder.append(
                "${index + 1}. ${track.artistName} - ${track.trackName} (${
                    SimpleDateFormat(
                        "mm:ss",
                        Locale.getDefault()
                    ).format(track.trackTimeMillis)
                })\n"
            )
        }
        return stringBuilder.toString()
    }

    private fun showSelectedPlaylist(playlist: PlayList) {
        binding.trackAmountSelectedPlaylist.text = countTracks(playlist.trackCount)
        binding.titleSelectedPlaylist.text = playlist.name
        binding.descriptionTextviewSelectedPlaylist.text = playlist.description

        val imageUri = playlist.imageUri.let { Uri.parse(it) }
        Glide.with(requireContext())
            .load(imageUri)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .into(binding.playlistImage)

        Glide.with(requireContext())
            .load(imageUri)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .into(binding.coverBottomSelectedPlaylist)
    }

    private fun countTracks(count: Int): String {
        val countTrack = context?.resources?.getQuantityString(R.plurals.count_tracks, count)
        return "$count $countTrack"
    }

    private fun showContent(tracks: List<Track>) {
        binding.rvSelectedPlaylist.visibility = View.VISIBLE
        adapter.updateList(tracks.toMutableList())
    }

    private fun openAudioPlayer(track: Track) {
        val intent = Intent(requireContext(), PlayerActivity::class.java)
        val trackJson = Gson().toJson(track)
        intent.putExtra(CURRENT_TRACK, trackJson)
        startActivity(intent)
    }

    private fun showDialog(trackId: Long) {
        MaterialAlertDialogBuilder(requireContext(), R.style.dialog)
            .setMessage(getString(R.string.isDeleteTrack))
            .setNegativeButton(getString(R.string.No)) { _, _ -> }
            .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                viewModel.deleteTrackFromPlaylist(trackId)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}