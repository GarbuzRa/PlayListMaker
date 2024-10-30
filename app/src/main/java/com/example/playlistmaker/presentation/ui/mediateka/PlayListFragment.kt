package com.example.playlistmaker.presentation.ui.mediateka

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.playlistmaker.databinding.FragmentPlayListBinding
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.PlaylistState
import com.example.playlistmaker.presentation.ui.main.MainActivity
import com.example.playlistmaker.presentation.ui.mediateka.adapter.PlaylistsAdapter
import com.example.playlistmaker.presentation.ui.mediateka.adapter.PlaylistsViewHolder
import com.example.playlistmaker.presentation.viewmodel.PlayListViewModel

class PlayListFragment : Fragment(), PlaylistsViewHolder.ClickListener {
    private var _binding: FragmentPlayListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PlaylistsAdapter

    private val viewModel by viewModel<PlayListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notFoundImg.visibility = View.GONE
        binding.noPlaylistText.visibility = View.GONE

        viewModel.getPlaylists()

        adapter = PlaylistsAdapter(this)
        binding.playlistsRV.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRV.adapter = adapter

        viewModel.playlistState.observe(viewLifecycleOwner) {
            observePlaylists(it)
        }

        binding.newPlayListButton.setOnClickListener {
            findNavController().navigate(R.id.action_media_to_new_playlist)
        }
    }

    private fun observePlaylists(state: PlaylistState) {
        when (state) {
            is PlaylistState.ShowPlaylists -> {
                val playlists = state.playlists
                showPlaylists()
                adapter.playlists = playlists as ArrayList<PlayList>
                adapter.notifyDataSetChanged()
                (activity as? MainActivity)?.setNavBarVis(true)
            }

            is PlaylistState.Empty -> {
                showNoPlaylists()
            }

            is PlaylistState.Load -> {
                binding.playlistsProgressBar.visibility = View.VISIBLE
            }

        }
    }

    private fun showPlaylists() {
        binding.playlistsProgressBar.visibility = View.GONE
        binding.notFoundImg.visibility = View.GONE
        binding.noPlaylistText.visibility = View.GONE
        binding.playlistsRV.visibility = View.VISIBLE
    }

    private fun showNoPlaylists() {
        binding.playlistsProgressBar.visibility = View.GONE
        binding.notFoundImg.visibility = View.VISIBLE
        binding.noPlaylistText.visibility = View.VISIBLE
        binding.playlistsRV.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlayListFragment().apply {
            arguments = Bundle().apply {
            }
        }
    }

    override fun onClick(playlist: PlayList) {
        val bundle = Bundle().apply {
            putParcelable("selected_playlist", playlist)
        }
        findNavController().navigate(
            R.id.action_mediatekaFragment_to_currentPlaylistFragment,
            bundle
        )

    }
}