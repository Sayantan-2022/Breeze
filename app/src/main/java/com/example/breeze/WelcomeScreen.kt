package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        val window : Window = window
        window.statusBarColor = getColor(R.color.welcome)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnCreate = findViewById<Button>(R.id.btnCreate)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
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

        btnCreate.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }
}