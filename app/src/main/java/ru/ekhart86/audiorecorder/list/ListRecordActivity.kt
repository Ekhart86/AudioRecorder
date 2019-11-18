package ru.ekhart86.audiorecorder.list

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recyclerview.*
import ru.ekhart86.audiorecorder.R
import ru.ekhart86.audiorecorder.sql.DBHelper

class ListRecordActivity : AppCompatActivity() {

    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recyclerview)
        //Добавляем заголовок
        val actionBar = supportActionBar
        actionBar!!.title = "Все записи"
        var recordsList: ArrayList<Record> = getFullListCommands()
        //Создание RecyclerView в Activity (recyclerViewRecordList это id RecyclerView)
        layoutManager = LinearLayoutManager(this)
        recyclerViewRecordList.layoutManager = layoutManager
        recyclerViewRecordList.adapter = RecordAdapter(this, recordsList)
    }

    private fun getFullListCommands(): ArrayList<Record> {
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
                list.add(
                    Record(
                        cursor.getInt(idIndex),
                        cursor.getString(dateIndex),
                        cursor.getString(valueIndex)
                    )
                )
            } while (cursor.moveToNext())
        } else
            Log.d("mLog", "0 rows")

        cursor.close()
        dbHelper.close()

        return list
    }
}