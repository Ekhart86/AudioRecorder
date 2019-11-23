package ru.ekhart86.audiorecorder.record

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ru.ekhart86.audiorecorder.R
import ru.ekhart86.audiorecorder.sql.DBHelper


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

    private lateinit var mStartRecordButton: ImageButton
    private lateinit var mStopRecordButton: ImageButton
    private lateinit var mPauseRecordButton: ImageButton
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var mOutputFile: String
    private lateinit var audioInputText: TextView
    private lateinit var frecuencySamplingText: TextView
    private var isPressedpause: Boolean = false
    private lateinit var chronometer: Chronometer
    private var timeWhenStopped: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        audioInputText = findViewById(R.id.inputSoundCurrent)
        frecuencySamplingText = findViewById(R.id.samplingFrequencyCurrent)
        mStartRecordButton = findViewById(R.id.start_record_button_id)
        mStopRecordButton = findViewById(R.id.stop_record_button_id)
        mPauseRecordButton = findViewById(R.id.pause_record_button_id)
        chronometer = findViewById(R.id.view_timer)

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
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        //Устанавливаем частоту в зависимости от частоты выбранной в настройках
        mediaRecorder!!.setAudioSamplingRate(currentFrecuencySampling)
        mediaRecorder!!.setAudioEncodingBitRate(384000)
        mediaRecorder!!.setOutputFile(mOutputFile)
        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
        mStartRecordButton.isEnabled = false
        mStopRecordButton.isEnabled = true
        mStartRecordButton.setColorFilter(Color.RED)
        chronometer.base = SystemClock.elapsedRealtime()
        timeWhenStopped = 0
        chronometer.start()
        var toast = makeText(applicationContext, "Запись началась", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.show()
    }

    //Остановить запись и сохранить результат в базу данных
    fun clickStopRecordButton(v: View) {
        chronometer.stop()
        mediaRecorder!!.stop()
        mediaRecorder!!.release()
        //Уничтожаем обьект рекордера после каждой записи
        mediaRecorder = null
        DBHelper.addRecordToDB(this, mOutputFile)
        mStartRecordButton.isEnabled = true
        mStopRecordButton.isEnabled = false
        mStartRecordButton.clearColorFilter()
        mPauseRecordButton.clearColorFilter()

        var toast = makeText(applicationContext, "Запись успешно завершена", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.show()
    }


    fun clickPauseRecordButton(view: View) {

        if (isPressedpause) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder!!.resume()
                mPauseRecordButton.clearColorFilter()
                isPressedpause = false
                chronometer.base = SystemClock.elapsedRealtime() + timeWhenStopped
                chronometer.start()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder!!.pause()
                timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime()
                chronometer.stop()
            }
            mPauseRecordButton.setColorFilter(Color.RED)
            isPressedpause = true
        }
    }


    //Уничтожить рекордер если запись не была завершена кнопкой стоп
    override fun onDestroy() {
        if (mediaRecorder != null) {
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            mediaRecorder = null
            makeText(applicationContext, "Запись прервана без сохранения", Toast.LENGTH_LONG)
                .show()
        }
        super.onDestroy()
    }


}

