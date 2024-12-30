package com.example.breeze

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val label = "Welcome to,\nBreeze"
        val stringBuilder = StringBuilder()

        Thread{
            for (letter in label) {
                stringBuilder.append(letter)
                Thread.sleep(100)
                runOnUiThread {
                    tvWelcome.text = stringBuilder.toString()
                }

            }
        }.start()
    }
}