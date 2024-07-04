package com.example.playlistmaker.presentation.ui.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.ui.player.PlayerActivity
import com.example.playlistmaker.util.Creator
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private val trackRepository = Creator.provideTrackRepository(this)
    private var tracksList = mutableListOf<Track>()
    private var historyTrackList = mutableListOf<Track>()
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
        setupAdapters()
        readHistory()
    }

    private fun setupViews() {
        binding.searchHistoryRecycleView.visibility = View.VISIBLE
        binding.trackRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.searchBackButton.setOnClickListener { finish() }
        binding.updateButton.setOnClickListener { onUpdateButtonClick() }
        binding.clearText.setOnClickListener { clearSearch() }
        binding.trackSearch.addTextChangedListener(createTextWatcher())
        binding.trackSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                trackSearch(binding.trackSearch.text.toString())
                true
            }
            false
        }
        binding.historyClearButton.setOnClickListener {
            trackRepository.clearSearchHistory()
            setLayoutVis(binding.searchHistoryLayout, false)
        }
    }

    private fun setupAdapters() {
        historyAdapter = TrackAdapter(historyTrackList) { track ->
            trackRepository.addToSearchHistory(track)
            debounceClick { gotoPlayer(track) }
        }
        binding.searchHistoryRecycleView.adapter = historyAdapter

        adapter = TrackAdapter(tracksList) { track ->
            trackRepository.addToSearchHistory(track)
            readHistory()
            debounceClick { gotoPlayer(track) }
        }
        binding.trackRecycler.adapter = adapter
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isEmpty()) {
                    binding.clearText.visibility = View.GONE
                    readHistory()
                    if (historyTrackList.isNotEmpty()) {
                        setLayoutVis(binding.searchHistoryLayout, true)
                    }
                } else {
                    setLayoutVis(binding.searchHistoryLayout, false)
                    binding.clearText.visibility = View.VISIBLE
                    adapter.clearList()
                    setLayoutVis(binding.notFoundLayout, false)
                    setLayoutVis(binding.noInternetLayout, false)
                    searchRunnable?.let { handler.removeCallbacks(it) }
                    searchRunnable = Runnable { trackSearch(s.toString()) }
                    handler.postDelayed(searchRunnable!!, 2000L)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun clearSearch() {
        binding.trackSearch.setText("")
        keyboardHide()
        adapter.clearList()
        readHistory()
        if (historyTrackList.isNotEmpty()) {
            setLayoutVis(binding.searchHistoryLayout, true)
        } else {
            setLayoutVis(binding.searchHistoryLayout, false)
        }
        setLayoutVis(binding.notFoundLayout, false)
        setLayoutVis(binding.noInternetLayout, false)
    }

    private fun keyboardHide() {
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { inputMethod.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun setLayoutVis(layout: View, vis: Boolean) {
        layout.visibility = if (vis) View.VISIBLE else View.GONE
    }

    private fun onUpdateButtonClick() {
        setLayoutVis(binding.notFoundLayout, false)
        setLayoutVis(binding.noInternetLayout, false)
        trackSearch(binding.trackSearch.text.toString())
    }

    private fun trackSearch(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        trackRepository.searchTracks(query) { result ->
            binding.progressBar.visibility = View.GONE
            result.fold(
                onSuccess = { tracks ->
                    if (tracks.isNotEmpty()) {
                        binding.trackRecycler.visibility = View.VISIBLE
                        tracksList.clear()
                        tracksList.addAll(tracks)
                        adapter.updateList(tracksList)
                        binding.trackRecycler.scrollToPosition(0)
                    } else {
                        adapter.clearList()
                        setLayoutVis(binding.noInternetLayout, false)
                        setLayoutVis(binding.notFoundLayout, true)
                    }
                },
                onFailure = {
                    adapter.clearList()
                    setLayoutVis(binding.noInternetLayout, true)
                    setLayoutVis(binding.notFoundLayout, false)
                }
            )
        }
    }

    private fun readHistory() {
        historyTrackList.clear()
        historyTrackList.addAll(trackRepository.getSearchHistory())
        historyAdapter.notifyDataSetChanged()
        if (historyTrackList.isNotEmpty()) {
            setLayoutVis(binding.searchHistoryLayout, true)
        }
    }

    private fun debounceClick(action: () -> Unit) {
        handler.postDelayed({ action() }, 300L)
    }

    private fun gotoPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(CURRENT_TRACK, Gson().toJson(track))
        startActivity(intent)
    }

    companion object {
        const val CURRENT_TRACK = "current_track"
    }
}