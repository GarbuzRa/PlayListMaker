package com.example.playlistmaker.presentation.ui.mediateka

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.playlistmaker.databinding.FragmentFavoriteTracksBinding
import com.example.playlistmaker.presentation.ui.player.PlayerActivity
import com.example.playlistmaker.presentation.ui.search.TrackAdapter
import com.example.playlistmaker.presentation.viewmodel.FavoritesViewModel
import com.example.playlistmaker.util.CURRENT_TRACK
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {
    private val viewModel by viewModel<FavoritesViewModel>()
    private lateinit var adapter : TrackAdapter
    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TrackAdapter(mutableListOf()){track ->
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra(CURRENT_TRACK, Gson().toJson(track))
            startActivity(intent)
        }
        binding.rvFavorites.adapter = adapter

        viewModel.favoriteTracks.observe(viewLifecycleOwner){tracks ->
            if (tracks.isEmpty()) {
                binding.emptyLayout.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            } else {
                binding.emptyLayout.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE
                adapter.updateList(tracks.toMutableList())
            }
        }

        viewModel.loadFavoriteTracks()
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}