package ru.ekhart86.audiorecorder.play

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import ru.ekhart86.audiorecorder.MainActivity
import ru.ekhart86.audiorecorder.R
import ru.ekhart86.audiorecorder.sql.DBHelper
import java.io.File
import java.io.IOException

private const val PLAY_TAG = "AudioPlay"

class PlayActivity : AppCompatActivity() {

    private lateinit var mDate: TextView
    private lateinit var mPlay: MaterialButton
    private var mAudioRecorder: MediaPlayer? = null
    private var recordValue: String? = null
    private var currentId: Int? = null
    private lateinit var dbHelper: DBHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        //Добавляем заголовок
        val actionBar = supportActionBar
        currentId = intent.getIntExtra("id", 0)
        actionBar!!.title = "Запись № $currentId"
        mDate = findViewById(R.id.record_date_detail_id)
        val dateString = intent.getStringExtra("date")
        mDate.text = dateString
        recordValue = intent.getStringExtra("value")
        mPlay = findViewById(R.id.play_button_id)

    }


    fun clickPlayButton(v: View) {
        var pathWrite = "${externalCacheDir!!.absolutePath}/audioPlay.3gp"
        File(pathWrite).writeBytes(decodeBase64(recordValue))
        mAudioRecorder = MediaPlayer()

        try {
            mAudioRecorder!!.setDataSource(pathWrite)
            mAudioRecorder!!.prepare()
            mAudioRecorder!!.start()
        } catch (e: IOException) {
            Log.e(PLAY_TAG, "Ошибка воспроизведения аудиозаписи.")
        }
    }

    //Декодируем строковое значение из base64 в массив байтов, для дальнейшей записи в кэш и воспроизведения
    private fun decodeBase64(value: String?): ByteArray {
        return Base64.decode(value, Base64.DEFAULT)
    }


    fun clickRemoveButton(v: View) {
        dbHelper = DBHelper(this)
        val database = dbHelper.writableDatabase
        val removeId = arrayOf(currentId.toString())
        database.delete(DBHelper.TABLE_RECORDS, "_id = ?", removeId)
        dbHelper.close()
        Toast.makeText(applicationContext, "Запись $currentId успешно удалена", Toast.LENGTH_LONG)
            .show()
        val intent = Intent(this@PlayActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}