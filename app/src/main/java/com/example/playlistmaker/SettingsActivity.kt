package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.Toast


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //вернуться назад
        val buttonBack = findViewById<Button>(R.id.button_back)
        buttonBack.setOnClickListener{
            finish()
        }

        val shareButton = findViewById<Button>(R.id.shareButton)
        shareButton.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            val url = getString(R.string.YPurl)
            intent.putExtra(Intent.EXTRA_TEXT, url)
            intent.type = "text/plain"
            startActivity(intent)
        }

        val supportButton = findViewById<Button>(R.id.supportButton)
        supportButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SENDTO
            intent.data = Uri.parse(getString(R.string.mailto_mymail))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.support_text))
            startActivity(intent)


        }
        val agreeButton = findViewById<Button>(R.id.agreeButton)
        agreeButton.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(getString(R.string.offer_uri))
            startActivity(intent)

        }

    }
}