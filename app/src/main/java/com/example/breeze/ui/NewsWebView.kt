package com.example.breeze.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.breeze.ProfileActivity
import com.example.breeze.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.FirebaseDatabase

class NewsWebView : AppCompatActivity() {

    lateinit var database: FirebaseDatabase
    @SuppressLint("SetJavaScriptEnabled", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_web_view)

        val window : Window = window
        window.statusBarColor = getColor(R.color.dark_blue)
        window.navigationBarColor = getColor(R.color.dark_blue)

        val uid = intent.getStringExtra("uid")
        val url = intent.getStringExtra("url")
        val btnProfile = findViewById<ShapeableImageView>(R.id.btnProfile)
        val webView = findViewById<WebView>(R.id.newsWebView)

        webView.webViewClient = WebViewClient()
        webView.apply {
            settings.javaScriptEnabled = true
            settings.safeBrowsingEnabled = true
            loadUrl(url.toString())
        }

        database = FirebaseDatabase.getInstance()
        if (uid != null) {
            database.getReference("Accounts").child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val imageUri = it.child("imageUri").value
                    if (imageUri != null && imageUri.toString().isNotEmpty())
                        Glide.with(this)
                            .load(imageUri.toString())
                            .placeholder(R.drawable.blank_profile_picture)
                            .error(R.drawable.blank_profile_picture)
                            .into(btnProfile)
                    else
                        btnProfile.setImageResource(R.drawable.blank_profile_picture)
                }
            }
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }
    }
}