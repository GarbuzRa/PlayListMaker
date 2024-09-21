// File: presentation/ui/search/SearchActivity.kt

package com.example.playlistmaker.presentation.ui.search

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.domain.model.SearchState
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.ui.player.PlayerActivity
import com.example.playlistmaker.presentation.viewmodel.SearchViewModel
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModel()
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        setupAdapters()
        observeViewModel()
    }

    private fun setupViews() {
        binding.searchHistoryRecycleView.visibility = View.VISIBLE
        binding.trackRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.updateButton.setOnClickListener { viewModel.searchTracks(binding.trackSearch.text.toString()) }
        binding.clearText.setOnClickListener { clearSearch() }
        binding.trackSearch.addTextChangedListener(createTextWatcher())
        binding.trackSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchTracks(binding.trackSearch.text.toString())
                true
            } else {
                false
            }
        }
        binding.historyClearButton.setOnClickListener { viewModel.clearSearchHistory() }
    }

    private fun setupAdapters() {
        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            gotoPlayer(track)
        }
        binding.searchHistoryRecycleView.adapter = historyAdapter

        adapter = TrackAdapter(mutableListOf()) { track ->
            viewModel.addToSearchHistory(track)
            gotoPlayer(track)
        }
        binding.trackRecycler.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            adapter.updateList(tracks.toMutableList())
            binding.trackRecycler.visibility = View.VISIBLE
        }

        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.ShowHistory -> {
                    if (binding.trackSearch.text.isNullOrEmpty()) {
                        binding.searchHistoryLayout.visibility = View.VISIBLE
                        binding.trackRecycler.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        binding.noInternetLayout.visibility = View.GONE
                        binding.notFoundLayout.visibility = View.GONE
                    }
                }
                is SearchState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.trackRecycler.visibility = View.GONE
                    binding.searchHistoryLayout.visibility = View.GONE
                    binding.noInternetLayout.visibility = View.GONE
                    binding.notFoundLayout.visibility = View.GONE
                }
                is SearchState.ShowSearchResults -> {
                    binding.trackRecycler.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.searchHistoryLayout.visibility = View.GONE
                    binding.noInternetLayout.visibility = View.GONE
                    binding.notFoundLayout.visibility = View.GONE
                    adapter.updateList(state.tracks.toMutableList())
                }
                is SearchState.Error -> {
                    binding.noInternetLayout.visibility = View.VISIBLE
                    binding.trackRecycler.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.searchHistoryLayout.visibility = View.GONE
                    binding.notFoundLayout.visibility = View.GONE
                }
                is SearchState.Empty -> {
                    binding.notFoundLayout.visibility = View.VISIBLE
                    binding.trackRecycler.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.searchHistoryLayout.visibility = View.GONE
                    binding.noInternetLayout.visibility = View.GONE
                }
            }
        }

        viewModel.historyTracks.observe(viewLifecycleOwner) { tracks ->
            historyAdapter.updateList(tracks.toMutableList())
            binding.searchHistoryLayout.isVisible = tracks.isNotEmpty() && binding.trackSearch.text.isNullOrEmpty()
            binding.historyClearButton.isVisible = tracks.isNotEmpty() && binding.trackSearch.text.isNullOrEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) binding.trackRecycler.visibility = View.GONE
        }

        viewModel.showError.observe(viewLifecycleOwner) { showError ->
            binding.noInternetLayout.visibility = if (showError) View.VISIBLE else View.GONE
        }

        viewModel.showEmpty.observe(viewLifecycleOwner) { showEmpty ->
            binding.notFoundLayout.visibility = if (showEmpty) View.VISIBLE else View.GONE
        }

        if (binding.trackSearch.text.isNullOrEmpty()) {
            viewModel.getSearchHistory()
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearText.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                if (s.isNullOrEmpty()) {
                    binding.trackRecycler.visibility = View.GONE
                    viewModel.getSearchHistory()
                } else {
                    binding.searchHistoryLayout.visibility = View.GONE
                    viewModel.searchTracks(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun clearSearch() {
        binding.trackSearch.setText("")
        keyboardHide()
        adapter.clearList()
        viewModel.getSearchHistory()
    }

    private fun keyboardHide() {
        val inputMethod = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let { inputMethod.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun gotoPlayer(track: Track) {
        val intent = Intent(requireContext(), PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.CURRENT_TRACK, Gson().toJson(track))
        startActivity(intent)
    }
}
