package ru.ekhart86.audiorecorder.record

import android.Manifest
import android.content.ContentValues
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
import java.text.SimpleDateFormat
import java.util.*


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecordActivity : AppCompatActivity() {

    val APP_PREFERENCES = "settings"
    val SELECTED_AUDIO_INPUT = "selectedAudioInput"
    val SELECTED__FRECUENCY = "selectedFrecuencySampling"
    lateinit var currentAudioInput: String
    lateinit var currentFrecuencySampling: String
    lateinit var preferences: SharedPreferences

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var mStartRecordButton: MaterialButton
    private lateinit var mStopRecordButton: MaterialButton
    private var myAudioRecorder: MediaRecorder? = null
    private lateinit var mOutputFile: String
    private lateinit var dbHelper: DBHelper
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
        actionBar!!.title = "Новая запись"
        //Путь до папки с кэшем
        mOutputFile = "${externalCacheDir!!.absolutePath}/audioRecord.mp4"

        ActivityCompat.requestPermissions(
            this, permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )
        //Получаем записанные в SharedPreferences радиобатоны, если ничего нет, то будут выбраны микрофон и среднее качество
        currentAudioInput =
            preferences.getString(SELECTED_AUDIO_INPUT, getString(R.string.microphone)).toString()
        currentFrecuencySampling =
            preferences.getString(SELECTED__FRECUENCY, getString(R.string.medium_frequency))
                .toString()
        audioInputText.text = currentAudioInput
        frecuencySamplingText.text = currentFrecuencySampling
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
        //Устанавливаем высокое качество частоты дискретизации если оно выбрано в настройках
        if (currentFrecuencySampling == getString(R.string.high_frecuency)) {
            myAudioRecorder!!.setAudioEncodingBitRate(384000)
            myAudioRecorder!!.setAudioSamplingRate(44100)
        }
        myAudioRecorder!!.setOutputFile(mOutputFile)
        myAudioRecorder!!.prepare()
        myAudioRecorder!!.start()
        mStartRecordButton.isEnabled = false
        mStopRecordButton.isEnabled = true
        Toast.makeText(applicationContext, "Запись началась", Toast.LENGTH_LONG).show()
    }

    //Остановить запись и сохранить результат в базу данных
    fun clickStopRecordButton(v: View) {
        dbHelper = DBHelper(this)
        myAudioRecorder!!.stop()
        myAudioRecorder!!.release()
        //Уничтожаем обьект рекордера после каждой записи
        myAudioRecorder = null
        val recordData = convertToBase64(mOutputFile)
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateTimeInstance()
        val formDate = formatter.format(date)

        val database = dbHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(DBHelper.KEY_VALUE, recordData)
        contentValues.put(DBHelper.KEY_DATE, formDate)
        database.insert(DBHelper.TABLE_RECORDS, null, contentValues)
        dbHelper.close()
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

