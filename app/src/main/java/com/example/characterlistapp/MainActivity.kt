package com.example.characterlistapp

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import android.util.TypedValue
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // 次に動的に生成するボタンに割り当てる一意のIDを追跡するカウンター
    private var nextButtonId = 1000

    // 最後に表示した削除ボタンを保持するための変数（一つだけ表示させるため）
    private var lastClickedDeleteButton: Button? = null

    /**
     * dp (Density-independent Pixels) を px (Pixels) に変換するヘルパー関数
     */
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val Createfile: TextView = findViewById<TextView>(R.id.NewCreateFile)
        val Createbutton: Button = findViewById<Button>(R.id.NewCreateFileButton)
        Createfile.isVisible = false
        Createbutton.isVisible = false

        // 起動時に既存のファイル名に基づいてボタンを全て生成する
        loadExistingButtons()

        //入力スポット切り替え
        val CreateMenuButton: Button = findViewById<Button>(R.id.Firstbutton)
        CreateMenuButton.setOnClickListener{
            Log.d("buttonmsg","Create Menu Button Clicked")
            Createfile.isVisible = !Createfile.isVisible
            Createbutton.isVisible = !Createbutton.isVisible
            val ClearText: EditText? = findViewById<EditText>(R.id.NewFileName)
            ClearText?.setText("")
        }

        //ファイル名決定
        val CreateButtom:Button = findViewById<Button>(R.id.NewCreateFileButton)
        CreateButtom.setOnClickListener{
            val FileNameId: EditText? = findViewById<EditText>(R.id.NewFileName)
            if(FileNameId != null) {
                val NewFilename: String = FileNameId.text.toString()
                if (NewFilename.isNotBlank()) {
                    SQLiteFile.addList(applicationContext, NewFilename)

                    val currentCount = SQLiteFile.getListItemCount(applicationContext)
                    Log.d("DB_COUNT", "現在のデータ数: $currentCount 件")

                    // 新しく追加された項目に対してボタンを生成
                    Create(NewFilename)

                    FileNameId.setText("")
                }
            }
        }
    }

    /**
     * データベースから全てのリスト名を取得し、ボタンとして画面に描画します。
     */
    private fun loadExistingButtons() {
        val listNames = SQLiteFile.getListName(applicationContext)

        // 取得したリスト名に基づいてボタンを生成
        for (name in listNames) {
            // Create 関数が nextButtonId をインクリメントしながら ID を割り当ててくれます
            Create(name)
        }
    }


    /**
     * 動的に「ファイル名ボタン」と「削除ボタン」のペアを作成し、メインコンテナに追加します。
     */
    fun Create(text: String){
        val mainContainer = findViewById<LinearLayout>(R.id.FileField)

        // 1. 水平方向のコンテナを作成 (ファイル名ボタンと削除ボタンを格納するため)
        val horizontalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            // 親コンテナ (FileField) の幅一杯に広げる
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(60) // ボタンの高さを固定 (例: 60dp)
            ).apply {
                topMargin = dpToPx(5) // 各行の縦の間隔
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        // 2. ファイル名表示ボタンの作成
        val texting = Button(this).apply {
            this.text = text
            this.gravity = Gravity.START // テキストを左寄せ
            this.textSize = 25F
            this.id = nextButtonId++
            // 幅をできるだけ広げる (Weight 1)
            layoutParams = LinearLayout.LayoutParams(
                0, // width: 0dp
                LinearLayout.LayoutParams.MATCH_PARENT, // height: match_parent
                1.0f // weight: 1.0f
            )
            // 背景色を設定
            val buttonColor = ContextCompat.getColor(context, android.R.color.holo_blue_light)
            backgroundTintList = ColorStateList.valueOf(buttonColor)
        }

        // 3. 削除ボタン (Delete Button) の作成
        val deleteButton = Button(this).apply {
            this.text = "✖"
            this.textSize = 18F
            this.isVisible = false // 初期状態では非表示
            // 削除ボタンの色を設定
            val deleteColor = ContextCompat.getColor(context, android.R.color.holo_red_light)
            backgroundTintList = ColorStateList.valueOf(deleteColor)
            // 幅を小さく固定
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(60), // width: 60dp
                LinearLayout.LayoutParams.MATCH_PARENT // height: match_parent
            )
        }

        // 4. ファイル名ボタンのクリックリスナー (削除ボタンの表示/非表示を切り替える)
        texting.setOnClickListener {
            // 最後に表示した削除ボタンがあれば非表示にする
            if (lastClickedDeleteButton != null && lastClickedDeleteButton != deleteButton) {
                lastClickedDeleteButton?.isVisible = false
            }

            // 現在の削除ボタンの表示状態をトグルする
            deleteButton.isVisible = !deleteButton.isVisible

            // 最後に表示したボタンを更新
            lastClickedDeleteButton = if (deleteButton.isVisible) deleteButton else null
        }

        // 5. 削除ボタンのクリックリスナー (データの削除と画面からの除去)
        deleteButton.setOnClickListener {
            val listNameToDelete = texting.text.toString()

            // データベースから削除
            val success = SQLiteFile.deleteList(applicationContext, listNameToDelete)

            if (success) {
                // 画面から親コンテナ（水平レイアウト）ごと除去
                mainContainer.removeView(horizontalLayout)
                Log.d("Delete", "データとボタンを削除しました: $listNameToDelete")
                // nextButtonId の管理は複雑になるため、ここでは省略します。
                lastClickedDeleteButton = null
            } else {
                Log.e("Delete", "データの削除に失敗しました: $listNameToDelete")
            }
        }

        // 6. ビューをコンテナに追加し、メインレイアウトに追加
        horizontalLayout.addView(texting)
        horizontalLayout.addView(deleteButton)
        mainContainer.addView(horizontalLayout)
    }
}