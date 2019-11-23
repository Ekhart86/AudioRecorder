package ru.ekhart86.audiorecorder

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import ru.ekhart86.audiorecorder.bluetooth.BluetoothRecordActivity
import ru.ekhart86.audiorecorder.list.ListRecordActivity
import ru.ekhart86.audiorecorder.record.RecordActivity
import ru.ekhart86.audiorecorder.settings.SettingsActivity


class MainActivity : AppCompatActivity() {

    lateinit var preferences: SharedPreferences
    val APP_PREFERENCES = "settings"
    val SELECTED_AUDIO_INPUT = "selectedAudioInput"
    lateinit var currentAudioInput: String

    private lateinit var createNewButton: MaterialButton
    private lateinit var listAudioButton: MaterialButton
    private lateinit var settingsButton: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        //Разместить заголовок главной страницы по центру
        val actionBar = supportActionBar
        actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar.setCustomView(R.layout.app_bar)
        createNewButton = findViewById(R.id.record_audio_button_id)
        listAudioButton = findViewById(R.id.audio_list_button_id)
        settingsButton = findViewById(R.id.settings_button_id)
    }


    fun clickCreateNewButton(v: View) {
        currentAudioInput =
            preferences.getString(SELECTED_AUDIO_INPUT, getString(R.string.microphone)).toString()

        if (currentAudioInput == getString(R.string.microphone)) {
            val intent = Intent(this@MainActivity, RecordActivity::class.java)
            startActivity(intent)
        } else if (currentAudioInput == getString(R.string.bluetooth)) {
            val intent = Intent(this@MainActivity, BluetoothRecordActivity::class.java)
            startActivity(intent)
        }
    }

    fun clickListAudioButton(v: View) {
        val intent = Intent(this@MainActivity, ListRecordActivity::class.java)
        startActivity(intent)
    }

    fun clickSettingsButton(v: View) {
        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(intent)
    }

}
