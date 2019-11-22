package ru.ekhart86.audiorecorder.record

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import ru.ekhart86.audiorecorder.R
import ru.ekhart86.audiorecorder.sql.DBHelper
import java.io.File


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecordActivity : AppCompatActivity() {

    val APP_PREFERENCES = "settings"
    val SELECTED_AUDIO_INPUT = "selectedAudioInput"
    val SELECTED__FRECUENCY = "selectedFrecuencySampling"
    lateinit var currentAudioInput: String
    var currentFrecuencySampling: Int = 22050
    lateinit var preferences: SharedPreferences

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var mStartRecordButton: MaterialButton
    private lateinit var mStopRecordButton: MaterialButton
    private var myAudioRecorder: MediaRecorder? = null
    private lateinit var mOutputFile: String
    private lateinit var audioInputText: TextView
    private lateinit var frecuencySamplingText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        audioInputText = findViewById(R.id.inputSoundCurrent)
        frecuencySamplingText = findViewById(R.id.samplingFrequencyCurrent)
        mStartRecordButton = findViewById(R.id.start_record_button_id)
        mStopRecordButton = findViewById(R.id.stop_record_button_id)

        //Добавляем заголовок
        val actionBar = supportActionBar
        actionBar!!.title = getString(R.string.new_record)
        //Путь до папки с кэшем
        mOutputFile = "${externalCacheDir!!.absolutePath}/audioRecord.mp4"

        ActivityCompat.requestPermissions(
            this, permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )
        //Получаем записанные в SharedPreferences радиобатоны, если ничего нет, то будут выбраны микрофон и 22050
        currentAudioInput =
            preferences.getString(SELECTED_AUDIO_INPUT, getString(R.string.microphone)).toString()
        currentFrecuencySampling =
            preferences.getInt(SELECTED__FRECUENCY, 22050)

        audioInputText.text = currentAudioInput
        frecuencySamplingText.text = currentFrecuencySampling.toString()
        mStopRecordButton.isEnabled = false
    }

    //Проверить есть ли разрешение от пользователя на запись аудио
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }


    fun clickStartRecordButton(v: View) {
        //Создаём обьект рекордера при каждой записи
        myAudioRecorder = MediaRecorder()
        myAudioRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        myAudioRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        myAudioRecorder!!.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        //Устанавливаем частоту в зависимости от частоты выбранной в настройках
        myAudioRecorder!!.setAudioSamplingRate(currentFrecuencySampling)
        myAudioRecorder!!.setAudioEncodingBitRate(384000)
        myAudioRecorder!!.setOutputFile(mOutputFile)
        myAudioRecorder!!.prepare()
        myAudioRecorder!!.start()
        mStartRecordButton.isEnabled = false
        mStopRecordButton.isEnabled = true
        Toast.makeText(applicationContext, "Запись началась", Toast.LENGTH_LONG).show()
    }

    //Остановить запись и сохранить результат в базу данных
    fun clickStopRecordButton(v: View) {
        myAudioRecorder!!.stop()
        myAudioRecorder!!.release()
        //Уничтожаем обьект рекордера после каждой записи
        myAudioRecorder = null
        DBHelper.addRecordToDB(this, mOutputFile)
        mStartRecordButton.isEnabled = true
        mStopRecordButton.isEnabled = false
        Toast.makeText(applicationContext, "Запись успешно завершена", Toast.LENGTH_LONG).show()
    }

    //Кодируем аудиозапись в base64 строку для возможности сохранения в базе данных
    private fun convertToBase64(path: String): String {
        var file = File(path)
        return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    }

    //Уничтожить рекордер если запись не была завершена кнопкой стоп
    override fun onDestroy() {
        if (myAudioRecorder != null) {
            myAudioRecorder!!.stop()
            myAudioRecorder!!.release()
            myAudioRecorder = null
            Toast.makeText(applicationContext, "Запись прервана без сохранения", Toast.LENGTH_LONG)
                .show()
        }
        super.onDestroy()
    }


}

