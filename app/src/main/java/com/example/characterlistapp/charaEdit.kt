package com.example.characterlistapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class charaEdit: AppCompatActivity() {

    private var characterId: Long = -1L
    private var parentListId: Long = -1L
    private var parentListName: String = "" // 👈 リスト名を追加

    private lateinit var nameEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.chara_edit_scene) // レイアウトファイル名が chara_edit_scene.xml と仮定
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mains)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. データの受け取り
        characterId = intent.getLongExtra("CHARACTER_ID", -1L) // 編集対象のキャラクターID
        parentListId = intent.getLongExtra("LIST_ID", -1L) // 所属リストID (戻るボタン用)
        parentListName = intent.getStringExtra("LIST_NAME") ?: "不明なリスト" // 👈 リスト名を受け取り保持

        if (characterId == -1L || parentListId == -1L) {
            Toast.makeText(this, "エラー: 編集対象の情報が不正です。", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. UI要素の初期化
        nameEditText = findViewById(R.id.edit_char_name) // 🚨 chara_edit_scene.xml の IDを確認
        contentEditText = findViewById(R.id.edit_char_content) // 🚨 chara_edit_scene.xml の IDを確認
        saveButton = findViewById(R.id.save_edit_button) // 🚨 chara_edit_scene.xml の IDを確認
        deleteButton = findViewById(R.id.delete_character_button) // 🚨 chara_edit_scene.xml の IDを確認

        // 3. 既存データの読み込みと表示
        loadCharacterData()

        // 4. 更新ボタンのリスナー設定
        saveButton.setOnClickListener {
            saveChanges()
        }

        // 5. 削除ボタンのリスナー設定
        deleteButton.setOnClickListener {
            deleteCharacter()
        }

        // 6. 戻るボタンの設定
        val backButton: Button = findViewById<Button>(R.id.BackButton) // 🚨 chara_edit_scene.xml の IDを確認
        backButton.setOnClickListener {
            navigateToOpenFile() // 戻る時もリスト名・IDを渡す
        }
    }

    private fun loadCharacterData() {
        val characterToEdit = SQLiteFile.getCharacterById(this, characterId)

        if (characterToEdit != null) {
            nameEditText.setText(characterToEdit.name)
            contentEditText.setText(characterToEdit.content)
            // parentListId は getCharacterById で取得したものを使用しても良いが、
            // Intentから渡されたものを信頼して使用する
        } else {
            Toast.makeText(this, "キャラクターデータが見つかりませんでした。", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun saveChanges() {
        val newName = nameEditText.text.toString().trim()
        val newContent = contentEditText.text.toString().trim()

        if (newName.isBlank()) {
            Toast.makeText(this, "名前は必須です。", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedCharacter = CharaData(
            id = characterId,
            Listid = parentListId,
            name = newName,
            content = newContent
        )

        val rowsAffected = SQLiteFile.updateCharacter(applicationContext, updatedCharacter)

        if (rowsAffected > 0) {
            Toast.makeText(this, "更新が完了しました。", Toast.LENGTH_SHORT).show()
            navigateToOpenFile()
        } else {
            Toast.makeText(this, "更新に失敗しました。", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCharacter() {
        // 🚨 削除確認ダイアログの表示が推奨されます 🚨

        val rowsAffected = SQLiteFile.deleteCharacter(applicationContext, characterId)

        if (rowsAffected > 0) {
            Toast.makeText(this, "キャラクターを削除しました。", Toast.LENGTH_SHORT).show()
            navigateToOpenFile()
        } else {
            Toast.makeText(this, "削除に失敗しました。", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * OpenFile (キャラクター一覧画面) に戻るためのヘルパー関数。リストIDとリスト名を渡します。
     */
    private fun navigateToOpenFile() {
        val intent = Intent(this, OpenFile::class.java).apply {
            putExtra("LIST_ID", parentListId)
            putExtra("LIST_NAME", parentListName) // 👈 これでファイル名がタイトルに表示されます
        }
        startActivity(intent)
        finish()
    }
}