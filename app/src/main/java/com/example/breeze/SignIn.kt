package com.example.breeze

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.breeze.util.InternetChecker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignIn : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        InternetChecker().checkInternet(this, lifecycle)

        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val etEmail = findViewById<TextInputEditText>(R.id.ipEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.ipPassword)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                readData(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun readData(email: String, password: String) {
        firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this, "Sign In Successful!", Toast.LENGTH_SHORT).show()

                    val uid = it.result.user?.uid.toString()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this,
                    "There is a problem from our side,\nPlease try again!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}