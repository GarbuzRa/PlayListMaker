package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.nio.file.FileVisitOption

class SearchActivity : AppCompatActivity() {
    lateinit var adapter: TrackAdapter
    lateinit var editText: EditText
    lateinit var trackRecycler: RecyclerView
    lateinit var notFoundLayout: LinearLayout
    lateinit var noInternetLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        trackRecycler = findViewById(R.id.track_recycler)
        val searchBackButton = findViewById<Button>(R.id.search_back_button)
        searchBackButton.setOnClickListener{
            finish()
        }
        editText = findViewById<EditText>(R.id.track_search)
        val clearButton = findViewById<ImageView>(R.id.clear_text)
        val updateButton = findViewById<Button>(R.id.update_button)
        updateButton.setOnClickListener {
            onUpdateButtonClick()
        }
        notFoundLayout = findViewById(R.id.not_found_layout)
        noInternetLayout = findViewById(R.id.no_internet_layout)

        val myTextWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
                if (s.toString().trim().isEmpty()){
                    clearButton.visibility = GONE
                } else {
                    clearButton.visibility = VISIBLE
                    adapter.clearList()
                    setLayoutVis(notFoundLayout, false)
                    setLayoutVis(noInternetLayout, false)
                }
            }
            override fun afterTextChanged(s: Editable?){ //пуста

            }
        }
        editText.addTextChangedListener(myTextWatcher)
        clearButton.setOnClickListener{
            editText.setText("")
            keyboardHide()
            adapter.clearList()
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



        adapter = TrackAdapter()
        trackRecycler.adapter = adapter
        trackRecycler.layoutManager = LinearLayoutManager(this)

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
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    searchResponse?.let {
                        if (it.resultCount > 0) {
                            adapter.updateList(it.results)
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
                    adapter.clearList()
                    setLayoutVis(noInternetLayout, true)
                    setLayoutVis(notFoundLayout, false)
            }
        })
    }

}