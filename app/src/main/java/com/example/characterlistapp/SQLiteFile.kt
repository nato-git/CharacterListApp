package com.example.characterlistapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues


class SQLiteFile(context: Context,databaseName: String, factory: SQLiteDatabase.CursorFactory?, version: Int):
    SQLiteOpenHelper(context,databaseName,factory,version) {
    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL("create table if not exists TitleFile(name text)")
    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            database?.execSQL("alter table TitleFile add column deleteFlag INTEGER default 0")
        }
    }

    companion object {
        fun addList(context: Context, CreateName: String) {
            val dbHelper = SQLiteFile(context, "TitleFile", null, 1)
            val database = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("name", CreateName)
            }
            database.insert("TitleFile", null, values)
            database.close()
        }
    }
}