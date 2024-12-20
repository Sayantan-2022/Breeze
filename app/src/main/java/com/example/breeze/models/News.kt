package com.example.breeze.models

data class News(
    val `data`: MutableList<Data>,
    val hitsPerPage: Int,
    val page: Int,
    val size: Int,
    val success: Boolean,
    val timeMs: Int,
    val totalHits: Int,
    val totalPages: Int
)