package com.example.characterlistapp

import kotlinx.serialization.Serializable
@Serializable
data class CharaData (
    val id: Long = 0,
    val Listid: Long,
    val name: String,
    val content: String?
)

@Serializable
data class ListInfo (
    val id: Long,
    val name: String
)