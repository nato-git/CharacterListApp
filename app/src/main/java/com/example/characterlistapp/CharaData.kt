package com.example.characterlistapp


data class CharaData (
    val id: Long = 0,
    val Listid: Long,
    val name: String,
    val content: String?
)

data class ListInfo (
    val id: Long,
    val name: String
)