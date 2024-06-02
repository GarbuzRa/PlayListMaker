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
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    lateinit var adapter: TrackAdapter
    lateinit var historyAdapter: TrackAdapter
    lateinit var editText: EditText
    lateinit var trackRecycler: RecyclerView
    lateinit var notFoundLayout: LinearLayout
    lateinit var noInternetLayout: LinearLayout
    lateinit var searchHistoryLayout: LinearLayout
    lateinit var searchHistoryRecyclerView: RecyclerView
    lateinit var historySharedPreferences: SharedPreferences
    lateinit var progressBar: ProgressBar
    private var tracksList = ArrayList<Track>()
    private var historyTrackList = ArrayList<Track>()
    private var handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private lateinit var searchHistoryService: SearchHistoryService



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        searchHistoryLayout = findViewById(R.id.search_history_layout)
        searchHistoryRecyclerView = findViewById(R.id.search_history_recycle_view)
        historySharedPreferences = getSharedPreferences(TRACK_HISTORY_FILENAME, MODE_PRIVATE)
        searchHistoryService = SearchHistoryService(historySharedPreferences)
        var clearHistoryButton = findViewById<Button>(R.id.history_clear_button)
        clearHistoryButton.setOnClickListener{
            searchHistoryService.clear()
            setLayoutVis(searchHistoryLayout, false)
        }

        trackRecycler = findViewById(R.id.track_recycler)
        val searchBackButton = findViewById<Button>(R.id.search_back_button)
        searchBackButton.setOnClickListener{
            finish()
        }
        editText = findViewById<EditText>(R.id.track_search)
        val clearButton = findViewById<ImageView>(R.id.clear_text)
        val updateButton = findViewById<Button>(R.id.update_button)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        updateButton.setOnClickListener {
            onUpdateButtonClick()
        }
        notFoundLayout = findViewById(R.id.not_found_layout)
        noInternetLayout = findViewById(R.id.no_internet_layout)

        val myTextWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
                if (s.toString().trim().isEmpty()){
                    clearButton.visibility = GONE
                    readHistory()
                    if(historyTrackList.isNotEmpty()){
                        setLayoutVis(searchHistoryLayout,true)
                    }
                } else {
                    setLayoutVis(searchHistoryLayout, false)
                    clearButton.visibility = VISIBLE
                    adapter.clearList()
                    setLayoutVis(notFoundLayout, false)
                    setLayoutVis(noInternetLayout, false)
                    searchRunnable?.let {handler.removeCallbacks(it)} //очистка задачи searchRunnable из хэндлера
                    searchRunnable = Runnable {trackSearch(s.toString())} //создали задачу(поиска) и поместили в searchRunnable
                    handler.postDelayed(searchRunnable!!, 2000L) //поместить задачу на 2000мс
                    //с 92-94 каждый раз когда пользователь вводит символ, отчитывается 500млс и запускается поиск
                }
            }
            override fun afterTextChanged(s: Editable?){}
        }
        editText.addTextChangedListener(myTextWatcher)
        clearButton.setOnClickListener{
            editText.setText("")
            keyboardHide()
            adapter.clearList()
            readHistory()
            if(historyTrackList.isNotEmpty()){
                setLayoutVis(searchHistoryLayout, true)
            }
            else{
                setLayoutVis(searchHistoryLayout, false)
            }
            setLayoutVis(notFoundLayout, false)
            setLayoutVis(noInternetLayout, false)
        }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                trackSearch(editText.text.toString())
                true
            }
            false
        }

        historyAdapter = TrackAdapter(historyTrackList){track ->
            searchHistoryService.add(track)
            debounceClick{gotoPlayer(track)} //обернули вызов фу-ии gotoPlayer в дебоунс
        }
        searchHistoryRecyclerView.adapter = historyAdapter

        adapter = TrackAdapter(tracksList){track ->
            searchHistoryService.add(track)
            readHistory()
            debounceClick {gotoPlayer(track)} //обернули вызов фу-ии gotoPlayer в дебоунc
        }

        trackRecycler.adapter = adapter
        trackRecycler.layoutManager = LinearLayoutManager(this)

        //запрос истории
        readHistory()
        if(historyTrackList.isNotEmpty()){
            setLayoutVis(searchHistoryLayout, true)
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
        setLayoutVis(notFoundLayout, false)
        setLayoutVis(noInternetLayout, false)
        trackSearch(editText.text.toString())
    }
    private fun trackSearch(query: String) {
        progressBar.visibility = VISIBLE
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
                progressBar.visibility = GONE
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    searchResponse?.let {
                        if (it.resultCount > 0) {
                            trackRecycler.visibility = VISIBLE
                            tracksList.clear()
                            tracksList.addAll(it.results)
                            adapter.updateList(tracksList)
                            trackRecycler.scrollToPosition(0)
                        } else {
                            adapter.clearList()
                            setLayoutVis(noInternetLayout, false)
                            setLayoutVis(notFoundLayout, true)
                        }
                    }
                } else {
                    adapter.clearList()
                    setLayoutVis(noInternetLayout, true)
                    setLayoutVis(notFoundLayout, false)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                progressBar.visibility = GONE
                adapter.clearList()
                setLayoutVis(noInternetLayout, true)
                setLayoutVis(notFoundLayout, false)
            }
        })
    }
    fun readHistory() {
        historyTrackList.clear()
        historyTrackList.addAll(searchHistoryService.read())
        historyAdapter.notifyItemRangeChanged(0, historyTrackList.size)
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