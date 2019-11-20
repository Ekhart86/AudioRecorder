package ru.ekhart86.audiorecorder.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import ru.ekhart86.audiorecorder.R

class SettingsActivity : AppCompatActivity() {

    val APP_PREFERENCES = "mysettings"
    val APP_AUDIO_INPUT = "input"
    val APP_FRECUENCY = "frecuency"

    lateinit var pref: SharedPreferences

    private lateinit var microphoneButton: RadioButton
    private lateinit var bluetoothButton: RadioButton
    private lateinit var mediumQualityFrequencyButton: RadioButton
    private lateinit var highQualityFrequencyButton: RadioButton

    private lateinit var soundInputGroup: RadioGroup
    private lateinit var frecuencyQualityGroup: RadioGroup


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        //Добавляем заголовок
        val actionBar = supportActionBar
        actionBar!!.title = "Настройки"

        pref = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)

        soundInputGroup = findViewById(R.id.soundInputGroup)
        frecuencyQualityGroup = findViewById(R.id.frecuencyQualityGroup)
        microphoneButton = findViewById(R.id.microphone)
        bluetoothButton = findViewById(R.id.bluetooth)
        mediumQualityFrequencyButton = findViewById(R.id.frequencyMedium)
        highQualityFrequencyButton = findViewById(R.id.highFrequency)
        setSoundInputButton()
        setFrecuencyQualityButton()
    }

    override fun onPause() {
        super.onPause()
        //Записываем какие радиобатоны выбраны
        val editor = pref.edit()
        editor.putInt(APP_AUDIO_INPUT, soundInputGroup.checkedRadioButtonId)
        editor.putInt(APP_FRECUENCY, frecuencyQualityGroup.checkedRadioButtonId)
        editor.apply()
    }


    //Если есть записанное id радиобатона источника звука то сделать его выбранным
    private fun setSoundInputButton() {
        var selectedSoundButtonID = pref.getInt(APP_AUDIO_INPUT, -1)

        if (selectedSoundButtonID != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedSoundButtonID)
            val selectedRadioButtonText = selectedRadioButton.text.toString()
            println("$selectedRadioButtonText ------------------------selected.")
            selectedRadioButton.isChecked = true
        } else {
            println("Nothing selected from sound input Radio Group.")
        }
    }

    //Если есть записанное id радиобатона частоты дискретизации то сделать его выбранным
    private fun setFrecuencyQualityButton() {

        var selectedFrequencyButtonID = pref.getInt(APP_FRECUENCY, -1)
        println(selectedFrequencyButtonID)
        if (selectedFrequencyButtonID != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedFrequencyButtonID)
            val selectedRadioButtonText = selectedRadioButton.text.toString()
            println("$selectedRadioButtonText ------------------------selected.")
            selectedRadioButton.isChecked = true
        } else {
            println("Nothing selected from frecuency Radio Group.")
        }
    }

}
