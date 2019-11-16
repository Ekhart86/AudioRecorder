package ru.ekhart86.audiorecorder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ru.ekhart86.audiorecorder.list.AudioListActivity
import ru.ekhart86.audiorecorder.record.RecordActivity
import ru.ekhart86.audiorecorder.settings.SettingsActivity


class MainActivity : AppCompatActivity() {

    private lateinit var mCreateNewButton: Button
    private lateinit var mListAudioButton: Button
    private lateinit var mSettingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCreateNewButton = findViewById(R.id.record_audio_button_id)
        mListAudioButton = findViewById(R.id.audio_list_button_id)
        mSettingsButton = findViewById(R.id.settings_button_id)
    }


    fun clickCreateNewButton(v: View) {
        val intent = Intent(this@MainActivity, RecordActivity::class.java)
        startActivity(intent)
    }

    fun clickListAudioButton(v: View) {
        val intent = Intent(this@MainActivity, AudioListActivity::class.java)
        startActivity(intent)
    }

    fun clickSettingsButton(v: View) {
        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(intent)
    }
}
