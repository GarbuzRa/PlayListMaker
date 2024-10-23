package com.example.playlistmaker.presentation.ui.mediateka

import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.playlistmaker.databinding.FragmentPlayListBinding
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.presentation.ui.mediateka.adapter.PlaylistsAdapter
import com.example.playlistmaker.presentation.ui.mediateka.adapter.PlaylistsViewHolder
import com.example.playlistmaker.presentation.viewmodel.PlayListViewModel

class PlayListFragment : Fragment(), PlaylistsViewHolder.ClickListener {
    val viewModel by viewModel<PlayListViewModel>()
    lateinit var adapter: PlaylistsAdapter
    var _binding: FragmentPlayListBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notFoundImg.visibility = View.GONE
        binding.noPlaylistText.visibility = View.GONE
        viewModel.getPlayLists()
        adapter = PlaylistsAdapter(this)
        binding.playlistsRV.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRV.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlayListFragment()
    }

    override fun onClick(playList: PlayList) {
         // TODO("Not yet implemented")
    }
}