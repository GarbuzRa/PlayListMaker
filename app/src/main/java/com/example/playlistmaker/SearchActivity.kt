package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
     private lateinit var binding: ActivitySearchBinding
    private var tracksList = ArrayList<Track>()
    private var historyTrackList = ArrayList<Track>()
    private var handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private lateinit var searchHistoryService: SearchHistoryService



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        searchHistoryService = SearchHistoryService(binding.historySharedPreferences)
        var clearHistoryButton = findViewById<Button>(R.id.history_clear_button)
        clearHistoryButton.setOnClickListener{
            searchHistoryService.clear()
            setLayoutVis(binding.searchHistoryLayout, false)
        }

        binding.searchBackButton.setOnClickListener{
            finish()
        }

        binding.updateButton.setOnClickListener {
            onUpdateButtonClick()
        }
        val myTextWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
                if (s.toString().trim().isEmpty()){
                    binding.clearButton.visibility = GONE
                    readHistory()
                    if(historyTrackList.isNotEmpty()){
                        setLayoutVis(binding.searchHistoryLayout,true)
                    }
                } else {
                    setLayoutVis(binding.searchHistoryLayout, false)
                    binding.clearButton.visibility = VISIBLE
                    binding.adapter.clearList()
                    setLayoutVis(binding.notFoundLayout, false)
                    setLayoutVis(binding.noInternetLayout, false)
                    searchRunnable?.let {handler.removeCallbacks(it)} //очистка задачи searchRunnable из хэндлера
                    searchRunnable = Runnable {trackSearch(s.toString())} //создали задачу(поиска) и поместили в searchRunnable
                    handler.postDelayed(searchRunnable!!, 2000L) //поместить задачу на 2000мс
                    //с 92-94 каждый раз когда пользователь вводит символ, отчитывается 500млс и запускается поиск
                }
            }
            override fun afterTextChanged(s: Editable?){}
        }
        binding.editText.addTextChangedListener(myTextWatcher)
        binding.clearButton.setOnClickListener{
            binding.editText.setText("")
            keyboardHide()
            binding.adapter.clearList()
            readHistory()
            if(historyTrackList.isNotEmpty()){
                setLayoutVis(binding.searchHistoryLayout, true)
            }
            else{
                setLayoutVis(binding.searchHistoryLayout, false)
            }
            setLayoutVis(binding.notFoundLayout, false)
            setLayoutVis(binding.noInternetLayout, false)
        }
        binding.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                trackSearch(binding.editText.text.toString())
                true
            }
            false
        }

        binding.historyAdapter = TrackAdapter(historyTrackList){track ->
            searchHistoryService.add(track)
            debounceClick{gotoPlayer(track)} //обернули вызов фу-ии gotoPlayer в дебоунс
        }
        binding.searchHistoryRecyclerView.adapter = historyAdapter

        binding.adapter = TrackAdapter(tracksList){track ->
            searchHistoryService.add(track)
            readHistory()
            debounceClick {gotoPlayer(track)} //обернули вызов фу-ии gotoPlayer в дебоунc
        }

        binding.trackRecycler.adapter = adapter
        binding.trackRecycler.layoutManager = LinearLayoutManager(this)

        //запрос истории
        readHistory()
        if(historyTrackList.isNotEmpty()){
            setLayoutVis(binding.searchHistoryLayout, true)
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SAVED_TEXT", editText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString("SAVED_TEXT", "")
        editText.setText(savedText)

    }

    private fun keyboardHide() {
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
    private fun setLayoutVis(layout: LinearLayout, vis: Boolean) {
        if (vis) {
            layout.visibility = VISIBLE
        } else {
            layout.visibility = GONE
        }
    }
    private fun onUpdateButtonClick() {
        setLayoutVis(binding.notFoundLayout, false)
        setLayoutVis(binding.noInternetLayout, false)
        trackSearch(binding.editText.text.toString())
    }
    private fun trackSearch(query: String) {
        binding.progressBar.visibility = VISIBLE
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ItunesApiService::class.java)
        val call = service.search(query)

        call.enqueue(object: Callback<SearchResponse>
        {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                binding.progressBar.visibility = GONE
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    searchResponse?.let {
                        if (it.resultCount > 0) {
                            binding.trackRecycler.visibility = VISIBLE
                            tracksList.clear()
                            tracksList.addAll(it.results)
                            binding.adapter.updateList(tracksList)
                            binding.trackRecycler.scrollToPosition(0)
                        } else {
                            adapter.clearList()
                            setLayoutVis(binding.noInternetLayout, false)
                            setLayoutVis(binding.notFoundLayout, true)
                        }
                    }
                } else {
                    binding.adapter.clearList()
                    setLayoutVis(binding.noInternetLayout, true)
                    setLayoutVis(binding.notFoundLayout, false)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                binding.progressBar.visibility = GONE
                adapter.clearList()
                setLayoutVis(binding.noInternetLayout, true)
                setLayoutVis(binding.notFoundLayout, false)
            }
        })
    }
    fun readHistory() {
        historyTrackList.clear()
        historyTrackList.addAll(searchHistoryService.read())
        binding.historyAdapter.notifyItemRangeChanged(0, historyTrackList.size)
        Log.e("myLog", "readHistory + $historyTrackList")
    }

     private fun debounceClick(action:() -> Unit) {
        handler.postDelayed({action()}, 300L)
    }

    private fun gotoPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(CURRENT_TRACK, Gson().toJson(track))
        startActivity(intent)
    }

}