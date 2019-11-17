package ru.ekhart86.audiorecorder.sql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "recordsDb"
        const val TABLE_RECORDS = "records"
        const val KEY_ID = "_id"
        const val KEY_VALUE = "value"
        const val KEY_DATE = "date"
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

