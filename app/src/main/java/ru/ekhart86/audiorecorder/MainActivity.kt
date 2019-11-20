package ru.ekhart86.audiorecorder

import android.content.Intent
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

    private lateinit var mCreateNewButton: MaterialButton
    private lateinit var mListAudioButton: MaterialButton
    private lateinit var mSettingsButton: MaterialButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Разместить заголовок главной страницы по центру
        val actionBar = supportActionBar
        actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar.setCustomView(R.layout.app_bar)
        mCreateNewButton = findViewById(R.id.record_audio_button_id)
        mListAudioButton = findViewById(R.id.audio_list_button_id)
        mSettingsButton = findViewById(R.id.settings_button_id)
    }


    fun clickCreateNewButton(v: View) {
        val intent = Intent(this@MainActivity, RecordActivity::class.java)
        startActivity(intent)
    }

    fun clickListAudioButton(v: View) {
        val intent = Intent(this@MainActivity, ListRecordActivity::class.java)
        startActivity(intent)
    }

    fun clickSettingsButton(v: View) {
        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun clickBluetoothButton(view: View) {
        val intent = Intent(this@MainActivity, BluetoothRecordActivity::class.java)
        startActivity(intent)
    }
}
