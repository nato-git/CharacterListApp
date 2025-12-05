// SQLiteFile.kt の最終的な内容

package com.example.characterlistapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SQLiteFile(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "character_list_db"
        private const val DATABASE_VERSION = 2

        // リストタイトルテーブル
        private const val TABLE_TITLE = "TitleFile"
        private const val COLUMN_LIST_ID = "list_id"
        private const val COLUMN_LIST_NAME = "name"

        // キャラクターデータテーブル
        private const val TABLE_CHARACTER = "CharaFile"
        private const val COLUMN_CHARA_ID = "chara_id"
        private const val COLUMN_CHARA_LIST_ID = "list_id"
        private const val COLUMN_CHARA_NAME = "name"
        private const val COLUMN_CHARA_CONTENT = "content"

        // 静的アクセス関数
        fun addList(context: Context, name: String): Long {
            return SQLiteFile(context).addListInternal(name)
        }
        fun getListInfos(context: Context): List<ListInfo> {
            return SQLiteFile(context).getListInfosInternal()
        }
        fun addCharacter(context: Context, chara: CharaData): Long {
            return SQLiteFile(context).addCharacterInternal(chara)
        }
        fun getCharactersByListId(context: Context, listId: Long): List<CharaData> {
            return SQLiteFile(context).getCharactersByListIdInternal(listId)
        }
        fun getCharacterById(context: Context, charaId: Long): CharaData? {
            return SQLiteFile(context).getCharacterByIdInternal(charaId)
        }
        fun updateCharacter(context: Context, character: CharaData): Int {
            return SQLiteFile(context).updateCharacterInternal(character)
        }
        fun deleteCharacter(context: Context, charaId: Long): Int {
            return SQLiteFile(context).deleteCharacterInternal(charaId)
        }

        // 👈 新しい静的アクセス関数
        fun deleteListAndCharacters(context: Context, listId: Long): Boolean {
            return SQLiteFile(context).deleteListAndCharactersInternal(listId)
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE $TABLE_TITLE (
                $COLUMN_LIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LIST_NAME TEXT NOT NULL UNIQUE
            )
        """)
        db?.execSQL("""
            CREATE TABLE $TABLE_CHARACTER (
                $COLUMN_CHARA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CHARA_LIST_ID INTEGER NOT NULL,
                $COLUMN_CHARA_NAME TEXT NOT NULL,
                $COLUMN_CHARA_CONTENT TEXT,
                FOREIGN KEY($COLUMN_CHARA_LIST_ID) REFERENCES $TABLE_TITLE($COLUMN_LIST_ID) ON DELETE CASCADE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHARACTER")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TITLE")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        db?.execSQL("PRAGMA foreign_keys=ON;") // 外部キー制約を有効化
    }

    // --- ListInfo (リストタイトル) の操作（内部関数） ---
    private fun addListInternal(name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply { put(COLUMN_LIST_NAME, name) }
        val newRowId = db.insert(TABLE_TITLE, null, values)
        db.close()
        return newRowId
    }

    private fun getListInfosInternal(): List<ListInfo> {
        val list = mutableListOf<ListInfo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_LIST_ID, $COLUMN_LIST_NAME FROM $TABLE_TITLE", null)

        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIndex = c.getColumnIndex(COLUMN_LIST_ID)
                val nameIndex = c.getColumnIndex(COLUMN_LIST_NAME)
                do {
                    if (idIndex != -1 && nameIndex != -1) {
                        list.add(ListInfo(c.getLong(idIndex), c.getString(nameIndex)))
                    }
                } while (c.moveToNext())
            }
        }
        db.close()
        return list
    }

    // --- CharaData (キャラクター) の操作（内部関数） ---
    private fun addCharacterInternal(chara: CharaData): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHARA_LIST_ID, chara.Listid)
            put(COLUMN_CHARA_NAME, chara.name)
            put(COLUMN_CHARA_CONTENT, chara.content)
        }
        val newRowId = db.insert(TABLE_CHARACTER, null, values)
        db.close()
        return newRowId
    }

    private fun getCharactersByListIdInternal(listId: Long): List<CharaData> {
        val charaList = mutableListOf<CharaData>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CHARACTER WHERE $COLUMN_CHARA_LIST_ID = ?",
            arrayOf(listId.toString())
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIndex = c.getColumnIndex(COLUMN_CHARA_ID)
                val nameIndex = c.getColumnIndex(COLUMN_CHARA_NAME)
                val contentIndex = c.getColumnIndex(COLUMN_CHARA_CONTENT)
                val listIdIndex = c.getColumnIndex(COLUMN_CHARA_LIST_ID)

                do {
                    if (idIndex != -1) {
                        charaList.add(CharaData(
                            id = c.getLong(idIndex),
                            Listid = c.getLong(listIdIndex),
                            name = c.getString(nameIndex),
                            content = c.getString(contentIndex)
                        ))
                    }
                } while (c.moveToNext())
            }
        }
        db.close()
        return charaList
    }

    private fun getCharacterByIdInternal(charaId: Long): CharaData? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CHARACTER WHERE $COLUMN_CHARA_ID = ?",
            arrayOf(charaId.toString())
        )
        var charaData: CharaData? = null

        cursor.use { c ->
            if (c.moveToFirst()) {
                val listIdIndex = c.getColumnIndex(COLUMN_CHARA_LIST_ID)
                val nameIndex = c.getColumnIndex(COLUMN_CHARA_NAME)
                val contentIndex = c.getColumnIndex(COLUMN_CHARA_CONTENT)

                if (listIdIndex != -1) {
                    charaData = CharaData(
                        id = charaId,
                        Listid = c.getLong(listIdIndex),
                        name = c.getString(nameIndex),
                        content = c.getString(contentIndex)
                    )
                }
            }
        }
        db.close()
        return charaData
    }

    private fun updateCharacterInternal(character: CharaData): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHARA_NAME, character.name)
            put(COLUMN_CHARA_CONTENT, character.content)
        }
        val rowsAffected = db.update(
            TABLE_CHARACTER, values,
            "$COLUMN_CHARA_ID = ?", arrayOf(character.id.toString())
        )
        db.close()
        return rowsAffected
    }

    private fun deleteCharacterInternal(charaId: Long): Int {
        val db = writableDatabase
        val rowsAffected = db.delete(
            TABLE_CHARACTER,
            "$COLUMN_CHARA_ID = ?", arrayOf(charaId.toString())
        )
        db.close()
        return rowsAffected
    }

    // 👈 修正されたリストと関連キャラクターの削除メソッド
    private fun deleteListAndCharactersInternal(listId: Long): Boolean {
        val db = writableDatabase // DbHelper(context).writableDatabase ではなく、直接使用
        var result = false

        db.beginTransaction()
        try {
            // 1. (ON DELETE CASCADE があるため不要だが、明示的な削除ロジック)
            // リストに属するすべてのキャラクターを削除
            db.delete(
                TABLE_CHARACTER, // TABLE_CHARACTERS -> TABLE_CHARACTER に修正
                "${COLUMN_CHARA_LIST_ID} = ?", // COLUMN_LIST_ID を使用
                arrayOf(listId.toString())
            )

            // 2. リスト自体を削除
            val listRowsDeleted = db.delete(
                TABLE_TITLE, // TABLE_LISTS -> TABLE_TITLE に修正
                "${COLUMN_LIST_ID} = ?", // COLUMN_ID -> COLUMN_LIST_ID に修正
                arrayOf(listId.toString())
            )

            if (listRowsDeleted > 0) {
                db.setTransactionSuccessful()
                result = true
            } else {
                result = false
            }
        } catch (e: Exception) {
            Log.e("SQLiteFile", "リスト削除エラー: ${e.message}")
            result = false
        } finally {
            db.endTransaction()
            db.close()
        }
        return result
    }
}