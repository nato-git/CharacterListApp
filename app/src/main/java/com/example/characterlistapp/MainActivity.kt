package com.example.characterlistapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    private lateinit var createListLayout: TextView
    private lateinit var newListNameEditText: EditText
    private lateinit var createListButton: Button
    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. UI要素の初期化
        listContainer = findViewById<LinearLayout>(R.id.FileField)
        createListLayout = findViewById<TextView>(R.id.NewCreateFile)
        newListNameEditText = findViewById<EditText>(R.id.NewFileName)
        createListButton = findViewById<Button>(R.id.NewCreateFileButton)
        val showCreateButton: Button = findViewById<Button>(R.id.Firstbutton)

        createListLayout.isVisible = false
        createListButton.isVisible = false

        // 2. 新規作成エリアの表示切り替え
        showCreateButton.setOnClickListener {
            createListLayout.isVisible = !createListLayout.isVisible
            createListButton.isVisible = !createListButton.isVisible
            if (createListLayout.isVisible) {
                newListNameEditText.setText("")
                newListNameEditText.requestFocus()
            }
        }

        // 3. リスト作成ボタンの処理
        createListButton.setOnClickListener {
            createNewList()
        }

        // 4. 既存リストの読み込みと表示
        loadExistingLists()
    }

    private fun createNewList() {
        val listName = newListNameEditText.text.toString().trim()
        if (listName.isBlank()) {
            Toast.makeText(this, "リスト名を入力してください。", Toast.LENGTH_SHORT).show()
            return
        }

        val newRowId = SQLiteFile.addList(applicationContext, listName)

        if (newRowId > 0) {
            Toast.makeText(this, "'$listName' を作成しました。", Toast.LENGTH_SHORT).show()
            createListLayout.isVisible = false
            createListButton.isVisible = !createListButton.isVisible
            newListNameEditText.setText("")
            loadExistingLists()
        } else {
            Toast.makeText(this, "リストの作成に失敗しました。", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * データベースからリストを取得し、Buttonとして listContainer に動的に追加します。
     */
    private fun loadExistingLists() {
        listContainer.removeAllViews() // 既存のViewを全て削除
        val listInfos = SQLiteFile.getListInfos(applicationContext)

        listInfos.forEach { listInfo ->

            // 1. 各リスト項目を保持するための水平方向のコンテナを作成
            val listRowLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL // ボタンと削除ボタンを横に並べる
                setPadding(0, 8, 0, 8) // 上下にパディングを設定
            }

            // 2. リスト名ボタンを作成 (幅を flexible に設定)
            val listButton = Button(this).apply {
                // 幅を0dpにし、weight(重み)を1に設定することで、削除ボタンの残りスペースいっぱいに広がるようにする
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.0f // 重み1.0で、削除ボタンより優先して幅を取る
                )
                text = listInfo.name // リスト名を設定
                textSize = 18f
                gravity = left
                backgroundTintList = ColorStateList.valueOf(Color.CYAN)
                setTextColor(resources.getColor(android.R.color.black))
            }

            // リスト名ボタンのクリックリスナー (OpenFileへ遷移)
            listButton.setOnClickListener {
                val intent = Intent(this, OpenFile::class.java).apply {
                    putExtra("LIST_ID", listInfo.id)
                    putExtra("LIST_NAME", listInfo.name)
                }
                startActivity(intent)
            }

            // 3. 削除ボタンを作成
            val deleteButton = Button(this).apply {
                // 幅と高さを WRAP_CONTENT に設定
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(5, 0, 0, 0) // リスト名ボタンとの間にマージンを設定
                }
                text = "削除" // ボタンテキスト
                textSize = 16f
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            }

            val shareButton = ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    setMargins(0,0,0,0)
                }
                setImageResource(R.drawable.share)
                setScaleType(ImageView.ScaleType.FIT_CENTER)
                backgroundTintList = ColorStateList.valueOf((ContextCompat.getColor(context,android.R.color.white)))
            }

            // 削除ボタンのクリックリスナー (確認ダイアログを表示)
            deleteButton.setOnClickListener {
                showDeleteConfirmationDialog(listInfo.id, listInfo.name)
            }

            // 共有ボタンのクリックリスナー
            shareButton.setOnClickListener {
                shareSendData(listInfo.id)
            }

            // 4. コンテナにボタンを追加
            listRowLayout.addView(listButton)
            listRowLayout.addView(deleteButton)
            listRowLayout.addView(shareButton)

            // 5. メインコンテナにリスト行を追加
            listContainer.addView(listRowLayout)
        }
    }

    /**
     * リスト削除の確認ダイアログを表示する
     */
    private fun showDeleteConfirmationDialog(listId: Long, listName: String) {
        AlertDialog.Builder(this)
            .setTitle("リスト削除の確認")
            .setMessage("本当にリスト「$listName」を削除しますか？\nこのリスト内のキャラクターデータもすべて削除されます。")
            .setPositiveButton("削除") { dialog, which ->
                // 削除処理を実行
                performDeleteList(listId, listName)
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    /**
     * 実際にリストと関連キャラクターを削除する処理
     */
    private fun performDeleteList(listId: Long, listName: String) {
        val success = SQLiteFile.deleteListAndCharacters(applicationContext, listId) // 仮定: SQLiteFileにこのメソッドがある

        if (success) {
            Toast.makeText(this, "リスト「$listName」と関連キャラクターを削除しました。", Toast.LENGTH_SHORT).show()
            loadExistingLists() // リストを再読み込みして画面を更新
        } else {
            Toast.makeText(this, "削除に失敗しました。", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadExistingLists()
    }

    /**
     * ファイルをjsonファイルに変換する処理
     */
    private fun shareSendData(listId: Long) {
        val listName = SQLiteFile.getListInfos(applicationContext).find { it.id == listId }?.name ?: "CharacterList"
        val fileName = "${listName.replace(" ", "_")}.json"

        try {
            // 1. データベースからデータを取得
            val characters = SQLiteFile.getCharactersByListId(applicationContext, listId)
            val jsonString = Json.encodeToString<List<CharaData>>(characters)

            val cachePath = File(cacheDir, "shared_files")
            if (!cachePath.exists()) cachePath.mkdirs() // ディレクトリがなければ作成

            val file = File(cachePath, fileName)
            FileWriter(file).use { writer ->
                writer.write(jsonString)
            }

            // 3. FileProviderを使ってURIを取得
            val contentUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider", // AndroidManifestで定義したauthoritiesと一致させる
                file
            )

            // 4. URIとファイルタイプを指定して共有インテントを作成
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri) // 👈 ファイルURIを渡す
                type = "application/json" // 👈 JSONファイルタイプを指定
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 受信側のアプリに読み取り権限を一時的に付与
            }

            // 5. 共有アプリ選択画面を起動
            startActivity(Intent.createChooser(shareIntent, "JSONファイルを共有"))

        } catch (e: Exception) {
            Toast.makeText(this, "共有ファイルの作成に失敗しました: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}