package com.example.apistock.data.entities


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class SymbolSearch(
    val description: String,
    val symbol: String
)