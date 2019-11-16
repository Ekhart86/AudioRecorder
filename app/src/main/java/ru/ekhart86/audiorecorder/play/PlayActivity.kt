package ru.ekhart86.audiorecorder.play

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.ekhart86.audiorecorder.R
import java.io.File
import java.io.IOException

private const val LOG_TAG = "AudioPlay"

class PlayActivity : AppCompatActivity() {

    private lateinit var mDate: TextView
    private var mAudioRecorder: MediaPlayer? = null
    private var recordValue: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        //Добавляем заголовок
        val actionBar = supportActionBar
        actionBar!!.title = "Запись № ${intent.getIntExtra("id", 0)}"
        mDate = findViewById(R.id.record_date_detail_id)
        val dateString = intent.getStringExtra("date")
        mDate.text = dateString
        recordValue = intent.getStringExtra("value")

    }


    fun clickPlayButton(v: View) {
        var pathWrite = "${externalCacheDir!!.absolutePath}/audioPlay.3gp"
        println()
        File(pathWrite).writeBytes(decodeBase64(recordValue))
        mAudioRecorder = MediaPlayer()

        try {
            mAudioRecorder!!.setDataSource(pathWrite)
            mAudioRecorder!!.prepare()
            mAudioRecorder!!.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Ошибка воспроизведения аудиозаписи.")
        }
    }

    //Декодируем строковое значение из base64 в массив байтов, для дальнейшей записи в кэш и воспроизведения
    private fun decodeBase64(value: String?): ByteArray {
        return Base64.decode(value, Base64.DEFAULT)
    }

}