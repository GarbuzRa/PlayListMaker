package com.example.playlistmaker.presentation.ui.playlist

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSelectedPlaylistBinding
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.ui.main.BottomPanelController
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
    private lateinit var playlist: PlayList
    private lateinit var callback: OnBackPressedCallback
    private val adapter = TrackAdapter(mutableListOf())

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
        playlist = (arguments?.getParcelable("selected_playlist") as? PlayList)!!

        adapter.clickListener= {
            openAudioPlayer(it)
        }
        adapter.longClickListener = {
            showDialog(playlist, it.trackId?.toLong() ?: 0)
        }

        (activity as? BottomPanelController)?.enableBottomPanel()

        binding.rvSelectedPlaylist.adapter = adapter

        showSelectedPlaylist()

        val playlistId = playlist.id
        val screenHeight = resources.displayMetrics.heightPixels
        val allowableHeight = (screenHeight * 0.25).toInt()
        val bottomSheetBehavior =
            BottomSheetBehavior.from(binding.bottomMenuSelectedPlaylist).apply {
                state = BottomSheetBehavior.STATE_HIDDEN
            }
        val bottomSheetBehaviorPlaylist =
            BottomSheetBehavior.from(binding.playlistBottomMenuTracks).apply {
                state = BottomSheetBehavior.STATE_COLLAPSED
            }

        bottomSheetBehaviorPlaylist.peekHeight = allowableHeight

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        bottomSheetBehaviorPlaylist.isHideable = false
                        binding.menuBottomSheetOverlay.visibility = View.GONE
                    }
                    else -> {
                        binding.menuBottomSheetOverlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        val bundle = Bundle().apply {
            putParcelable("modify_playlist", playlist)
        }
        binding.editMenuSelectedPlaylist.setOnClickListener {
            findNavController().navigate(
                R.id.action_selectedPlaylistFragment_to_modifyPlaylistFragment,
                bundle
            )
        }

        binding.toolbarSelectedPlaylist.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.shareSelectedPlaylist.setOnClickListener {
            toShare()
        }

        binding.shareMenuSelectedPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            toShare()
        }

        binding.deleteMenuSelectedPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            toDelete()
        }

        binding.menuDotsSelectedPlaylist.setOnClickListener {
            binding.bottomMenuSelectedPlaylist.visibility = View.VISIBLE
            binding.titleBottomSelectedPlaylist.text = playlist.name
            binding.trackAmountBottomSelectedPlaylist.text = countTracks(playlist.trackCount)
            binding.menuBottomSheetOverlay.visibility = View.VISIBLE

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.menuBottomSheetOverlay.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.menuBottomSheetOverlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.menuBottomSheetOverlay.alpha = slideOffset
            }
        })

        binding.menuBottomSheetOverlay.setOnClickListener{
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        viewModel.getPlaylistById(playlistId)

        viewModel.observeTrackCount().observe(viewLifecycleOwner) { trackCount ->
            binding.trackAmountSelectedPlaylist.text = countTracks(trackCount)
            checkTracks()
        }

        viewModel.observePlaylistAllTime().observe(viewLifecycleOwner) {
            viewModel.playlistAllTime()
            if (it != null) {
                renderDuration(it)
            }
        }

        viewModel.observePlaylistId().observe(viewLifecycleOwner) {
            playlist = it
            viewModel.playlistAllTime()
            viewModel.getAllTracks(playlist.tracksId)
        }

        viewModel.observePlaylistTracks().observe(viewLifecycleOwner) {
            if (it != null) {
                showContent(it)
                viewModel.playlistAllTime()
            }
        }
    }

    fun checkTracks(){
        if (playlist.trackCount == 0) {
            binding.noTracksInPlaylist.visibility = View.VISIBLE
        } else {
            binding.noTracksInPlaylist.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        checkTracks()

        val playlistId = playlist.id
        viewModel.getPlaylistById(playlistId)
        viewModel.observePlaylistId().observe(viewLifecycleOwner) {
            playlist = it
            viewModel.getAllTracks(playlist.tracksId)
            viewModel.playlistAllTime()
            showSelectedPlaylist()
        }
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
                resources.getQuantityString(R.plurals.minutes, totalMinutes)
            }
        }
        binding.timeSelectedPlaylist.text = formattedDuration
    }

    private fun toDelete() {
        MaterialAlertDialogBuilder(requireContext(), R.style.dialog)
            .setMessage(getString(R.string.isDeletePlaylist))
            .setNegativeButton(getString(R.string.No)) { dialog, witch -> }
            .setPositiveButton(getString(R.string.Yes)) { dialog, witch ->
                viewModel.deletePlaylist()
                findNavController().navigateUp()
            }.show()
        checkTracks()
    }

    private fun toShare() {
        val tracks = viewModel.observePlaylistTracks().value ?: emptyList()
        if (tracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.noPlaylistToShare),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val message = formMessage()
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    }

    private fun formMessage(): String {
        val name = playlist.name
        val descript = playlist.description
        val tracks = viewModel.observePlaylistTracks().value ?: emptyList()
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

    private fun showSelectedPlaylist() {
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

    private fun showDialog(playlist: PlayList, trackId: Long) {
        MaterialAlertDialogBuilder(requireContext(), R.style.dialog)
            .setMessage(getString(R.string.isDeleteTrack))
            .setNegativeButton(getString(R.string.No)) { dialog, witch -> }
            .setPositiveButton(getString(R.string.Yes)) { dialog, witch ->
                viewModel.deleteTrackFromPlaylist(playlist, trackId)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::callback.isInitialized) {
            callback.remove()
        }
    }
}