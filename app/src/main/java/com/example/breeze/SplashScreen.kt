package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        val tvName = findViewById<TextView>(R.id.tvName)
        val lottie = findViewById<LottieAnimationView>(R.id.lottieAnim)

        lottie.animate().setDuration(2000)
        val label = "Breeze"
        val stringBuilder = StringBuilder()
        Thread{
            for (letter in label) {
                stringBuilder.append(letter)
                Thread.sleep(200)
                runOnUiThread {
                    tvName.text = stringBuilder.toString()
                }
            }
        }.start()

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            if (firebaseAuth.currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, WelcomeScreen::class.java)
                startActivity(intent)
            }
            finish()
        }, 2000)
    }
}