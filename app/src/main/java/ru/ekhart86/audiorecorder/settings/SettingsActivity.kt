package ru.ekhart86.audiorecorder.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import ru.ekhart86.audiorecorder.R

private const val SETTINGS_LOG_TAG = "settingsLog"

class SettingsActivity : AppCompatActivity() {

    val APP_PREFERENCES = "settings"
    val AUDIO_INPUT_BUTTON = "inputSound"
    val FRECUENCY_BUTTON = "frecuencySampling"
    val SELECTED_AUDIO_INPUT = "selectedAudioInput"
    val SELECTED__FRECUENCY = "selectedFrecuencySampling"

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
        editor.putInt(AUDIO_INPUT_BUTTON, soundInputGroup.checkedRadioButtonId)
        editor.putInt(FRECUENCY_BUTTON, frecuencyQualityGroup.checkedRadioButtonId)
        editor.apply()
    }

    //Записываем в SharedPreferences строками выбранные радиобатоны
    fun clickMicrophoneButton(view: View) {
        val editor = pref.edit()
        editor.putString(SELECTED_AUDIO_INPUT, microphoneButton.text.toString())
        editor.apply()
    }

    fun clickBluetoothButton(view: View) {
        val editor = pref.edit()
        editor.putString(SELECTED_AUDIO_INPUT, bluetoothButton.text.toString())
        editor.apply()
    }


    fun clickMediumSamplingButton(view: View) {
        val editor = pref.edit()
        editor.putString(SELECTED__FRECUENCY, mediumQualityFrequencyButton.text.toString())
        editor.apply()
    }

    fun clickHighSamplingButton(view: View) {
        val editor = pref.edit()
        editor.putString(SELECTED__FRECUENCY, highQualityFrequencyButton.text.toString())
        editor.apply()
    }


    //Если есть записанное id радиобатона источника звука то сделать его выбранным
    private fun setSoundInputButton() {

        var selectedSoundButtonID = pref.getInt(AUDIO_INPUT_BUTTON, -1)
        if (selectedSoundButtonID != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedSoundButtonID)
            val selectedRadioButtonText = selectedRadioButton.text.toString()
            Log.i(SETTINGS_LOG_TAG, "Выбран $selectedRadioButtonText")
            selectedRadioButton.isChecked = true
        } else {
            Log.i(SETTINGS_LOG_TAG, "Нет записей о выбранных радиобатоннах источника звука")
        }
    }

    //Если есть записанное id радиобатона частоты дискретизации то сделать его выбранным
    private fun setFrecuencyQualityButton() {

        var selectedFrequencyButtonID = pref.getInt(FRECUENCY_BUTTON, -1)
        if (selectedFrequencyButtonID != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedFrequencyButtonID)
            val selectedRadioButtonText = selectedRadioButton.text.toString()
            Log.i(SETTINGS_LOG_TAG, "Выбран $selectedRadioButtonText")
            selectedRadioButton.isChecked = true
        } else {
            Log.i(SETTINGS_LOG_TAG, "Нет записей о выбранных радиобатоннах частоты дискретизации")
        }
    }
}
