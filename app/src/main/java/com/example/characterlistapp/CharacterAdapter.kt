package com.example.characterlistapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//

class CharacterAdapter(
    private var characters: List<CharaData>, // 表示するデータリスト
    private val onItemClicked: (Long) -> Unit // クリックされたときの処理（キャラクターIDを返す）
) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    // 1. 各リストアイテムのビューを保持する ViewHolder クラス
    inner class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.character_name) // 👈 リスト行レイアウトに必要
        val contentTextView: TextView = itemView.findViewById(R.id.character_content) // 👈 リスト行レイアウトに必要

        fun bind(character: CharaData) {
            nameTextView.text = character.name
            contentTextView.text = character.content

            // アイテム全体がクリックされたときのリスナー
            itemView.setOnClickListener {
                onItemClicked(character.id)
            }
        }
    }

    // 2. リスト行のレイアウトを読み込み、ViewHolderを作成する
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        // 👈 character_list_item.xml というレイアウトファイルが必要です
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.character_list_item, parent, false)
        return CharacterViewHolder(view)
    }

    // 3. データと ViewHolder を結びつける（データをUIに反映）
    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(characters[position])
    }

    // 4. データリストのサイズを返す
    override fun getItemCount(): Int = characters.size

    // 5. データを更新するためのメソッド
    fun updateData(newCharacters: List<CharaData>) {
        characters = newCharacters
        notifyDataSetChanged() // データを変更したことを RecyclerView に通知し、再描画させる
    }
}