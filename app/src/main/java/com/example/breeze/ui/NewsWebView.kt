package com.example.breeze.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.breeze.R

class NewsWebView : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news_web_view)
        val url = intent.getStringExtra("url")
        val webView = findViewById<WebView>(R.id.newsWebView)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url!!)
    }
}