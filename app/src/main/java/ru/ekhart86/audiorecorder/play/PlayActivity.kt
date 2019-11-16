package ru.ekhart86.audiorecorder.play

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.ekhart86.audiorecorder.R

class PlayActivity : AppCompatActivity() {

    private lateinit var mRecordHeader: TextView
    private lateinit var mDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        mRecordHeader = findViewById(R.id.record_header_detail_id)
        mDate = findViewById(R.id.record_date_detail_id)
        val idInt = intent.getIntExtra("id", 0)
        mRecordHeader.text = "Запись № $idInt"
        val dateString = intent.getStringExtra("date")
        mDate.text = dateString
    }
}