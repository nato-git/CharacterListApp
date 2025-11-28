package com.example.characterlistapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class SQLiteFile(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // データベース作成時に一度だけ実行されます
    override fun onCreate(database: SQLiteDatabase?) {
        val createTableSQL = "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COLUMN_NAME TEXT)"
        database?.execSQL(createTableSQL)
    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            database?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_DELETE_FLAG INTEGER DEFAULT 0")
        }
    }

    companion object {
        // 定数
        const val DATABASE_NAME = "CharacterDB"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "TitleFile"
        const val COLUMN_NAME = "name"
        const val COLUMN_DELETE_FLAG = "deleteFlag"

        /**
         * データベースに新しいリスト名を追加します。
         */
        fun addList(context: Context, CreateName: String) {
            val dbHelper = SQLiteFile(context)
            val database = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_NAME, CreateName)
            }

            database.insert(TABLE_NAME, null, values)
            database.close()
        }

        // 🔴 新しく追加した関数: データ数を取得
        /**
         * データベース内の全リストの件数を取得します。
         */
        fun getListItemCount(context: Context): Int {
            val dbHelper = SQLiteFile(context)
            // 読み取り専用でデータベースを開きます
            val database = dbHelper.readableDatabase

            var count = 0
            // SELECT COUNT(*) FROM TitleFile クエリを実行します
            val cursor = database.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)

            // カーソルを最初の行に移動し、結果を取得します
            if (cursor.moveToFirst()) {
                // COUNT(*) の結果は0番目のカラムに入っています
                count = cursor.getInt(0)
            }

            // カーソルとデータベース接続を閉じます
            cursor.close()
            database.close()
            return count
        }
    }
}