package com.example.breeze.models

data class Bookmark(val title : String,
                    val imageUrl : String,
                    val excerpt : String,
                    val url : String,
                    val publisherName : String,
                    val publisherIcon : String,
                    var key : String? = null)
