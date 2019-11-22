package ru.ekhart86.audiorecorder.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "recordsDb"
        const val TABLE_RECORDS = "records"
        const val KEY_ID = "_id"
        const val KEY_VALUE = "value"
        const val KEY_DATE = "date"

        fun addRecordToDB(context: Context, pathToFile: String) {
            var dbHelper = DBHelper(context)
            val recordData = convertToBase64(pathToFile)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            val formDate = formatter.format(date)
            val database = dbHelper.writableDatabase
            val contentValues = ContentValues()
            contentValues.put(KEY_VALUE, recordData)
            contentValues.put(KEY_DATE, formDate)
            database.insert(TABLE_RECORDS, null, contentValues)
            dbHelper.close()
        }

        private fun convertToBase64(path: String): String {
            var file = File(path)
            return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        }
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table " + TABLE_RECORDS + "(" + KEY_ID
                    + " integer primary key," + KEY_VALUE + " text," + KEY_DATE + " text" + ")"
        )

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists $TABLE_RECORDS")
        onCreate(db)
    }


}

