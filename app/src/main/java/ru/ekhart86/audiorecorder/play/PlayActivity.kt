package ru.ekhart86.audiorecorder.play

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ru.ekhart86.audiorecorder.MainActivity
import ru.ekhart86.audiorecorder.R
import ru.ekhart86.audiorecorder.sql.DBHelper
import java.io.File
import java.io.IOException


private const val PLAY_TAG = "AudioPlay"

class PlayActivity : AppCompatActivity() {

    private lateinit var mDate: TextView
    private lateinit var playBtn: ImageButton
    private var mediaPlayer: MediaPlayer? = null
    private var currentId: Int? = null
    private var pathWrite = ""
    private lateinit var dbHelper: DBHelper
    private lateinit var chronometer: Chronometer
    private var timeWhenStopped: Long = 0

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
        playBtn = findViewById(R.id.play_button_id)
        chronometer = findViewById(R.id.view_timer_play)
        //Путь куда запишется файл из базы
        pathWrite = "${externalCacheDir!!.absolutePath}/audioPlay.mp4"
        File(pathWrite).writeBytes(
            decodeBase64(
                DBHelper.getCurrentRecord(
                    this,
                    currentId
                )!!.value
            )
        )

    }

    fun clickPlayButton(v: View) {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer!!.setDataSource(pathWrite)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            playBtn.setColorFilter(Color.RED)
            chronometer.base = SystemClock.elapsedRealtime()
            timeWhenStopped = 0
            chronometer.start()
            mediaPlayer!!.setOnCompletionListener {
                playBtn.isEnabled = true
                playBtn.clearColorFilter()
                chronometer.stop()
            }
        } catch (e: IOException) {
            Log.e(PLAY_TAG, "Ошибка воспроизведения аудиозаписи.")
        }
    }

    fun clickStopPlayButton(view: View) {

        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            chronometer.stop()
            mediaPlayer!!.stop()
            playBtn.clearColorFilter()
            mediaPlayer = null
        }
    }


    fun clickRemoveButton(v: View) {

        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Предупреждение")
        alertDialog.setMessage("Вы точно хотите удалить запись № $currentId?")
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Да") { _, _ ->
            dbHelper = DBHelper(this)
            val database = dbHelper.writableDatabase
            val removeId = arrayOf(currentId.toString())
            database.delete(DBHelper.TABLE_RECORDS, "_id = ?", removeId)
            dbHelper.close()
            Toast.makeText(
                applicationContext,
                "Запись $currentId успешно удалена",
                Toast.LENGTH_LONG
            )
                .show()
            val intent = Intent(this@PlayActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Нет"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()

        val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val layoutParams = btnPositive.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 5f
        layoutParams.leftMargin = 20
        btnPositive.layoutParams = layoutParams
        btnNegative.layoutParams = layoutParams
    }


    //Декодируем строковое значение из base64 в массив байтов, для дальнейшей записи в кэш и воспроизведения
    private fun decodeBase64(value: String?): ByteArray {
        return Base64.decode(value, Base64.DEFAULT)
    }

    //Остановить и уничтожить плеер при выходе с активити, если воспроизведение не была завершено
    override fun onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer = null
        }
        finish()
        super.onDestroy()
    }
}