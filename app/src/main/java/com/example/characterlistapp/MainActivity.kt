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
import java.io.File

class MainActivity : AppCompatActivity() {
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
        Createfile.isVisible = false    //入力スポット隠す
        Createbutton.isVisible = false

        //入力スポット切り替え
        val CreateMenuButton: Button = findViewById<Button>(R.id.Firstbutton)
        CreateMenuButton.setOnClickListener{
            Log.d("buttonmsg","This is a Button")
            Createfile.isVisible = !Createfile.isVisible
            Createbutton.isVisible = !Createbutton.isVisible
            val ClearText: EditText? = findViewById<EditText>(R.id.NewFileName)
            if(ClearText != null) {
                ClearText.setText("")
            }
        }

        //ファイル名決定
        val CreateButtom:Button = findViewById<Button>(R.id.NewCreateFileButton)
        CreateButtom.setOnClickListener{
            val FileNameId: EditText? = findViewById<EditText>(R.id.NewFileName)
            if(FileNameId != null) {
                val NewFilename: String = FileNameId.text.toString()
                SQLiteFile.addList(applicationContext, NewFilename)
                Create(NewFilename)
                FileNameId.setText("")
            }
        }
    }

    fun Create(text: String){
        val layout = findViewById<LinearLayout>(R.id.main)
        layout.gravity = Gravity.CENTER
        val texting = TextView(this)
        texting.text = text
        texting.gravity = Gravity.CENTER
        texting.textSize = 25F
        layout.addView(texting)
    }
}