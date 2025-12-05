package com.example.characterlistapp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import android.util.Log // デバッグ用に追加

class OpenFile : AppCompatActivity() {

    private var currentListId: Long = -1L
    private var currentListName: String = ""

    // UI 要素の宣言 (XML IDに合わせて調整)
    private lateinit var characterContainer: LinearLayout // 既存キャラ表示エリア (XMLでは id="@id/character_recycler_view" の LinearLayout)

    // 新規追加フォーム関連
    private lateinit var addCharaLayout: TextView         // 新規追加フォームの親View (XMLでは id="@id/createText" の TextView)
    private lateinit var newCharaNameEditText: EditText   // キャラ名入力 (XMLでは id="@id/NewTextName")
    private lateinit var newCharaContentEditText: EditText// 内容入力 (XMLでは id="@id/NewTextContent")
    private lateinit var confirmAddButton: Button         // 作成実行ボタン (XMLでは id="@id/NewCreateTextButton")

    // ボタン
    private lateinit var addButton: Button                // 新規追加トグルボタン (XMLでは id="@id/AddCharacterButton")
    private lateinit var backToListsButton: Button        // 戻るボタン (XMLでは id="@id/BackButton")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // レイアウトの親IDが @+id/files のため、それを使用
        setContentView(R.layout.open_file_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.files)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 渡されたリスト ID と名前を取得
        currentListId = intent.getLongExtra("LIST_ID", -1L)
        currentListName = intent.getStringExtra("LIST_NAME") ?: "不明なリスト"

        if (currentListId == -1L) {
            Toast.makeText(this, "エラー: リスト情報が不正です。", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. UI要素の初期化とタイトル設定
        val titleName: TextView = findViewById(R.id.Filename)
        titleName.text = currentListName

        // 3. UI要素のマッピング (XML IDに合わせて修正)
        characterContainer = findViewById(R.id.character_recycler_view)

        // XMLでは新規追加フォームの親 View が TextView(createText) として定義されています。
        // レイアウトを非表示にするため、このTextViewを変数に格納します。
        addCharaLayout = findViewById(R.id.createText)
        newCharaNameEditText = findViewById(R.id.NewTextName)
        newCharaContentEditText = findViewById(R.id.NewTextContent)
        confirmAddButton = findViewById(R.id.NewCreateTextButton)

        addButton = findViewById(R.id.AddCharacterButton) // ＋ボタン
        backToListsButton = findViewById(R.id.BackButton) // ＜ボタン

        // 4. 初期状態の設定
        // XML内の新規作成エリアはすべて初期状態で非表示にする
        setAddCharaVisibility(false)

        // 5. リスナー設定

        // 新規キャラクター追加ボタン (トグル機能)
        addButton.setOnClickListener {
            // 現在の状態の逆を設定
            setAddCharaVisibility(!addCharaLayout.isVisible)
        }

        // 新規キャラクター追加実行ボタン
        confirmAddButton.setOnClickListener {
            addCharacter()
        }

        // メイン画面に戻るボタン
        backToListsButton.setOnClickListener {
            finish() // MainActivityに戻る
        }
    }

    override fun onResume() {
        super.onResume()
        // 画面が再表示されるたびにキャラクター一覧を最新の情報に更新
        loadCharacters()
    }

    /**
     * 新規追加フォームの表示状態を制御するヘルパー関数
     */
    private fun setAddCharaVisibility(isVisible: Boolean) {
        addCharaLayout.isVisible = isVisible
        newCharaNameEditText.isVisible = isVisible
        newCharaContentEditText.isVisible = isVisible
        confirmAddButton.isVisible = isVisible
        findViewById<TextView>(R.id.Newcontent).isVisible = isVisible // "内容を作成" TextView

        if (isVisible) {
            newCharaNameEditText.setText("")
            newCharaContentEditText.setText("")
            newCharaNameEditText.requestFocus()
        }
    }


    /**
     * 新規キャラクターをデータベースに追加する
     */
    private fun addCharacter() {
        val name = newCharaNameEditText.text.toString().trim()
        val content = newCharaContentEditText.text.toString().trim()

        if (name.isBlank()) {
            Toast.makeText(this, "キャラクター名を入力してください。", Toast.LENGTH_SHORT).show()
            return
        }

        val newChara = CharaData(
            Listid = currentListId,
            name = name,
            content = if (content.isEmpty()) null else content
        )

        val newRowId = SQLiteFile.addCharacter(applicationContext, newChara)

        if (newRowId > 0) {
            Toast.makeText(this, "'$name' を追加しました。", Toast.LENGTH_SHORT).show()

            // フォームを非表示にして一覧を更新
            setAddCharaVisibility(false)
            loadCharacters()
        } else {
            Toast.makeText(this, "追加に失敗しました。", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * データベースからキャラクターデータを読み込み、LinearLayoutに動的に追加して表示する
     */
    private fun loadCharacters() {
        characterContainer.removeAllViews() // 既存のViewを全て削除

        val characterList = SQLiteFile.getCharactersByListId(this, currentListId)

        if (characterList.isEmpty()) {
            val emptyMessage = TextView(this).apply {
                text = "キャラクターがいません。追加ボタンで作成してください。"
                gravity = Gravity.CENTER
                textSize = 16f
                setPadding(0, 50, 0, 0)
            }
            characterContainer.addView(emptyMessage)
            return
        }

        characterList.forEach { chara ->
            // キャラクター名を表示するための TextView を動的に作成
            val charaView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 5, 0, 0)
                }
                text = chara.name
                textSize = 30f
                setPadding(10, 5, 10, 5)

                // 項目がタップされたときのリスナー
                setOnClickListener {
                    navigateToCharaEdit(chara.id, currentListId, currentListName)
                }
            }
            characterContainer.addView(charaView) // コンテナに追加
        }
    }

    /**
     * キャラクター編集画面 (charaEdit) へ遷移
     */
    private fun navigateToCharaEdit(charaId: Long, listId: Long, listName: String) {
        val intent = Intent(this, charaEdit::class.java).apply {
            putExtra("CHARACTER_ID", charaId)
            putExtra("LIST_ID", listId)
            putExtra("LIST_NAME", listName)
        }
        startActivity(intent)
    }
}