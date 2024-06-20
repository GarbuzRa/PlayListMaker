package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.playlistmaker.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //вернуться назад

        binding.buttonBack.setOnClickListener {
            finish()
        }


        binding.shareButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            val url = getString(R.string.YPurl)
            intent.putExtra(Intent.EXTRA_TEXT, url)
            intent.type = "text/plain"
            startActivity(intent)
        }


        binding.supportButton.setOnClickListener {
            val mailSubject = getString(R.string.support_subject)
            val mailText = getString(R.string.support_text)
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.mailto_mymail)))
            intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject)
            intent.putExtra(Intent.EXTRA_TEXT, mailText)
            startActivity(intent)
        }

        binding.agreeButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(getString(R.string.offer_uri))
            startActivity(intent)

        }

        //темная тема
        binding.themeSwitch.isChecked = (applicationContext as AppSettings).checkMode()
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            (applicationContext as AppSettings).themeToggle(isChecked)
            val sharedPref = getSharedPreferences(APP_SETTINGS_FILENAME, MODE_PRIVATE)
            sharedPref.edit()
                .putBoolean(IS_DARK_THEME_KEY, isChecked)
                .apply()
        }

    }
}