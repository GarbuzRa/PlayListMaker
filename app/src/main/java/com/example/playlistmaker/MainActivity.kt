package com.example.playlistmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val search_button = findViewById<Button>(R.id.search_button)
        search_button.setOnClickListener{
            val searchIntent = Intent (this, SearchActivity::class.java)
            startActivity(searchIntent)
        }

        val library_button = findViewById<Button>(R.id.library_button)
        library_button.setOnClickListener{
            val libraryIntent = Intent(this, LibraryActivity::class.java)
            startActivity(libraryIntent)
        }

        val settings_button = findViewById<Button>(R.id.settings_button)
        settings_button.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)

        }

    }

}