package ru.ekhart86.audiorecorder.record

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ru.ekhart86.audiorecorder.sql.DBHelper
import ru.ekhart86.audiorecorder.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecordActivity : AppCompatActivity() {


    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var mStartRecordButton: Button
    private lateinit var mStopRecordButton: Button
    private var myAudioRecorder: MediaRecorder? = null
    private lateinit var mOutputFile: String
    private lateinit var dbHelper: DBHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        //Добавляем заголовок
        val actionBar = supportActionBar
        actionBar!!.title = "Новая запись"
        //Путь до папки с кэшем
        mOutputFile = "${externalCacheDir!!.absolutePath}/audioRecord.3gp"

        ActivityCompat.requestPermissions(
            this, permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )
        mStartRecordButton = findViewById(R.id.start_record_button_id)
        mStopRecordButton = findViewById(R.id.stop_record_button_id)

        mStopRecordButton.isEnabled = false
        mStopRecordButton.setBackgroundColor(resources.getColor(R.color.colorGray))
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
        myAudioRecorder!!.setOutputFile(mOutputFile)
        myAudioRecorder!!.prepare()
        myAudioRecorder!!.start()
        mStartRecordButton.isEnabled = false
        mStopRecordButton.isEnabled = true
        mStartRecordButton.setBackgroundColor(resources.getColor(R.color.colorGray))
        mStopRecordButton.setBackgroundColor(resources.getColor(R.color.colorBlue))
        Toast.makeText(applicationContext, "Запись началась", Toast.LENGTH_LONG).show()
    }

    //Остановить запись и сохранить результат в базу данных
    fun clickStopRecordButton(v: View) {
        dbHelper = DBHelper(this)
        myAudioRecorder!!.stop()
        myAudioRecorder!!.release()
        //Уничтожаем обьект рекордера после каждой записи
        myAudioRecorder = null
        mStartRecordButton.isEnabled = true
        mStopRecordButton.isEnabled = false

        mStartRecordButton.setBackgroundColor(resources.getColor(R.color.colorBlue))
        mStopRecordButton.setBackgroundColor(resources.getColor(R.color.colorGray))

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
        Toast.makeText(applicationContext, "Запись успешно завершена", Toast.LENGTH_LONG)
            .show()
    }

    //Кодируем аудиозапись в base64 строку для возможности сохранения в базе данных
    private fun convertToBase64(path: String): String {
        var file = File(path)
        return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    }


}

