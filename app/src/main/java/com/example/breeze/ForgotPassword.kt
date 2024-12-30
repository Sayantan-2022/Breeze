package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.breeze.util.InternetChecker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ForgotPassword : AppCompatActivity() {

    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        InternetChecker().checkInternet(this, lifecycle)

        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val etEmail = findViewById<TextInputEditText>(R.id.ipEmail)

        btnChangePassword.setOnClickListener {
            val email = etEmail.text?.toString()

            firebaseAuth = FirebaseAuth.getInstance()

            if (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            Toast.makeText(this, "Password reset link sent to your email!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, SignIn::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
                            finish()
                        } else {
                            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter a valid Email ID!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}