package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val searchBackButton = findViewById<Button>(R.id.search_back_button)
        searchBackButton.setOnClickListener{
            finish()
        }
        val trackSearch = findViewById<EditText>(R.id.track_search)
        val clearButton = findViewById<ImageView>(R.id.clear_text)

        val myTextWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
                if (s.toString().trim().isEmpty()){
                    clearButton.visibility = GONE
                } else {
                    clearButton.visibility = VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?){ //пуста

            }
        }
        trackSearch.addTextChangedListener(myTextWatcher)
        clearButton.setOnClickListener{
            trackSearch.setText("")
            keyboardHide()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val editText = findViewById<EditText>(R.id.track_search)
        outState.putString("SAVED_TEXT", editText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString("SAVED_TEXT", "")
        val editText = findViewById<EditText>(R.id.track_search)
        editText.setText(savedText)

    }

    private fun keyboardHide() {
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}