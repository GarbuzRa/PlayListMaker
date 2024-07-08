// File: presentation/ui/search/SearchActivity.kt

package com.example.playlistmaker.presentation.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.ui.player.PlayerActivity
import com.example.playlistmaker.presentation.viewmodel.SearchViewModel
import com.example.playlistmaker.presentation.viewmodel.SearchViewModelFactory
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, SearchViewModelFactory()).get(SearchViewModel::class.java)

        setupViews()
        setupListeners()
        setupAdapters()
        observeViewModel()
    }

    private fun setupViews() {
        binding.searchHistoryRecycleView.visibility = View.VISIBLE
        binding.trackRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.searchBackButton.setOnClickListener { finish() }
        binding.updateButton.setOnClickListener { viewModel.searchTracks(binding.trackSearch.text.toString()) }
        binding.clearText.setOnClickListener { clearSearch() }
        binding.trackSearch.addTextChangedListener(createTextWatcher())
        binding.trackSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchTracks(binding.trackSearch.text.toString())
                true
            }
            false
        }
        binding.historyClearButton.setOnClickListener { viewModel.clearSearchHistory() }
    }

    private fun setupAdapters() {
        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            viewModel.addToSearchHistory(track)
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
        viewModel.tracks.observe(this) { tracks ->
            adapter.updateList(tracks.toMutableList())
            binding.trackRecycler.visibility = View.VISIBLE
        }

        viewModel.historyTracks.observe(this) { tracks ->
            historyAdapter.updateList(tracks.toMutableList())
            binding.searchHistoryLayout.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading)binding.trackRecycler.visibility = View.GONE
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.showError.observe(this) { showError ->
            binding.noInternetLayout.visibility = if (showError) View.VISIBLE else View.GONE
        }

        viewModel.showEmpty.observe(this) { showEmpty ->
            binding.notFoundLayout.visibility = if (showEmpty) View.VISIBLE else View.GONE
        }

        viewModel.getSearchHistory()
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
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { inputMethod.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun gotoPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.CURRENT_TRACK, Gson().toJson(track))
        startActivity(intent)
    }
}