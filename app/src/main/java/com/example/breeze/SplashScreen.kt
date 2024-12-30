package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            if (firebaseAuth.currentUser != null) {
                // User is already signed in, navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                // User is not signed in, navigate to SignUp
                val intent = Intent(this, WelcomeScreen::class.java)
                startActivity(intent)
            }
            finish()
        }, 2000)
    }
}