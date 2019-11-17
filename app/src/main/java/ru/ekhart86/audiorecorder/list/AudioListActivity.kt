package ru.ekhart86.audiorecorder.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import ru.ekhart86.audiorecorder.sql.DBHelper
import ru.ekhart86.audiorecorder.R
import ru.ekhart86.audiorecorder.play.PlayActivity
import java.util.*


class AudioListActivity : AppCompatActivity() {
    private val listRecords = ArrayList<Record>()
    private lateinit var listViewRecords: ListView
    private lateinit var selectedRecord: Record
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_audio)
        //Добавляем заголовок
        val actionBar = supportActionBar
        actionBar!!.title = "Все записи"
        listRecords.addAll(getFullListCommands())
        // получаем элемент ListView
        listViewRecords = findViewById(R.id.all_audio_records)
        // создаем адаптер
        val stateAdapter = RecordAdapter(this, R.layout.list_item, listRecords)
        // устанавливаем адаптер
        listViewRecords.adapter = stateAdapter
        // слушатель выбора в списке
        val itemListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            // получаем выбранный пункт
            selectedRecord = parent.getItemAtPosition(position) as Record
            val intent =
                Intent(this@AudioListActivity, PlayActivity::class.java)
            intent.putExtra("id", selectedRecord.id)
            intent.putExtra("value", selectedRecord.value)
            intent.putExtra("date", selectedRecord.date)
            startActivity(intent)
        }
        listViewRecords.onItemClickListener = itemListener
        getFullListCommands()
    }

    private fun getFullListCommands(): List<Record> {
        dbHelper = DBHelper(this)
        val database = dbHelper.writableDatabase
        val list = ArrayList<Record>()
        val cursor = database.query(DBHelper.TABLE_RECORDS, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(DBHelper.KEY_ID)
            val valueIndex = cursor.getColumnIndex(DBHelper.KEY_VALUE)
            val dateIndex = cursor.getColumnIndex(DBHelper.KEY_DATE)
            println(dateIndex)
            do {
                Log.d(
                    "mLog",
                    "ID = ${cursor.getInt(idIndex)} + , date = ${cursor.getString(dateIndex)}"
                )
                list.add(Record(cursor.getInt(idIndex), cursor.getString(dateIndex),cursor.getString(valueIndex)))
            } while (cursor.moveToNext())
        } else
            Log.d("mLog", "0 rows")

        cursor.close()
        dbHelper.close()

        return list
    }

}