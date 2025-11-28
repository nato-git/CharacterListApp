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
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    // dp (Density-independent Pixels) を px (Pixels) に変換するヘルパー関数
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

        //入力スポット切り替え
        val CreateMenuButton: Button = findViewById<Button>(R.id.Firstbutton)
        CreateMenuButton.setOnClickListener{
            Log.d("buttonmsg","This is a Button")
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

                    // データ数をログに出力
                    val currentCount = SQLiteFile.getListItemCount(applicationContext)
                    Log.d("DB_COUNT", "現在のデータ数: $currentCount 件")

                    Create(NewFilename)
                    FileNameId.setText("")
                }
            }
        }
    }

    /**
     * 動的にボタンを作成し、LinearLayoutコンテナに追加します。
     */
    fun Create(text: String){
        // 縦並びのコンテナ (R.id.FileField) を取得
        val layout = findViewById<LinearLayout>(R.id.FileField)

        val texting = Button(this)
        texting.text = text
        texting.gravity = Gravity.START
        texting.textSize = 25F

        // LinearLayout 用の LayoutParams を作成し、マージンを設定
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            // 各ボタンの上に 10dp のマージンを設定
            //topMargin = dpToPx(5)
            // コンテナ内で水平中央に配置
            gravity = Gravity.CENTER
        }

        texting.layoutParams = params

        // コンテナに追加すると、XMLの orientation="vertical" に従って縦に並びます
        layout.addView(texting)
    }
}