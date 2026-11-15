package com.example.chariiptv.domain

data class Channel(
    val name: String,
    val logo: String,
    val url: String,
    val country: String = "",
    val category: String = ""
)
