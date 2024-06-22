package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    lateinit var adapter: TrackAdapter
    lateinit var historyAdapter: TrackAdapter
    lateinit var historySharedPreferences: SharedPreferences
    lateinit var searchHistoryService: SearchHistoryService
    private var tracksList = ArrayList<Track>()
    private var historyTrackList = ArrayList<Track>()
    private var handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchHistoryRecycleView.visibility = View.VISIBLE
        historySharedPreferences = getSharedPreferences(TRACK_HISTORY_FILENAME, MODE_PRIVATE)
        searchHistoryService = SearchHistoryService(historySharedPreferences)
        binding.historyClearButton.setOnClickListener {
            searchHistoryService.clear()
            setLayoutVis(binding.searchHistoryLayout, false)
        }

        binding.trackRecycler.layoutManager = LinearLayoutManager(this)
        binding.searchBackButton.setOnClickListener {
            finish()
        }
        binding.updateButton.setOnClickListener {
            onUpdateButtonClick()
        }

        val myTextWatcher = object : TextWatcher {
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
        binding.trackSearch.addTextChangedListener(myTextWatcher)
        binding.clearText.setOnClickListener {
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
        binding.trackSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                trackSearch(binding.trackSearch.text.toString())
                true
            }
            false
        }

        historyAdapter = TrackAdapter(historyTrackList) { track ->
            searchHistoryService.add(track)
            debounceClick { gotoPlayer(track) }
        }
        binding.searchHistoryRecycleView.adapter = historyAdapter

        adapter = TrackAdapter(tracksList) { track ->
            searchHistoryService.add(track)
            readHistory()
            debounceClick { gotoPlayer(track) }
        }

        binding.trackRecycler.adapter = adapter

        readHistory()
        if (historyTrackList.isNotEmpty()) {
            setLayoutVis(binding.searchHistoryLayout, true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SAVED_TEXT", binding.trackSearch.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString("SAVED_TEXT", "")
        binding.trackSearch.setText(savedText)
    }

    private fun keyboardHide() {
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
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
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ItunesApiService::class.java)
        val call = service.search(query)

        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    searchResponse?.let {
                        if (it.resultCount > 0) {
                            binding.trackRecycler.visibility = View.VISIBLE
                            tracksList.clear()
                            tracksList.addAll(it.results)
                            adapter.updateList(tracksList)
                            binding.trackRecycler.scrollToPosition(0)
                        } else {
                            adapter.clearList()
                            setLayoutVis(binding.noInternetLayout, false)
                            setLayoutVis(binding.notFoundLayout, true)
                        }
                    }
                } else {
                    adapter.clearList()
                    setLayoutVis(binding.noInternetLayout, true)
                    setLayoutVis(binding.notFoundLayout, false)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                adapter.clearList()
                setLayoutVis(binding.noInternetLayout, true)
                setLayoutVis(binding.notFoundLayout, false)
            }
        })
    }

    fun readHistory() {
        historyTrackList.clear()
        historyTrackList.addAll(searchHistoryService.read())
        historyAdapter.notifyItemRangeChanged(0, historyTrackList.size)
        Log.e("myLog", "readHistory + $historyTrackList")
    }

    private fun debounceClick(action: () -> Unit) {
        handler.postDelayed({ action() }, 300L)
    }

    private fun gotoPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(CURRENT_TRACK, Gson().toJson(track))
        startActivity(intent)
    }
}