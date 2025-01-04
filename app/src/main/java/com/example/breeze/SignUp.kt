package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.breeze.util.Account
import com.example.breeze.util.InternetChecker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    
    private lateinit var database : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        InternetChecker().checkInternet(this, lifecycle)

        val window : Window = window
        window.statusBarColor = getColor(R.color.dark_blue)
        window.navigationBarColor = getColor(R.color.orange)

        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        val etName = findViewById<TextInputEditText>(R.id.ipName)
        val etEmail = findViewById<TextInputEditText>(R.id.ipEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.ipPassword)

        btnSignUp.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            val account = Account(name, email)

            database = FirebaseDatabase.getInstance().getReference("Accounts")
            firebaseAuth = FirebaseAuth.getInstance()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()

                                val uid = it.result.user?.uid.toString()
                                database.child(uid).setValue(account)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("uid", uid)
                                startActivity(intent)
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                finish()
                            } else {
                                exceptionHandler(it.exception)
                            }
                        }
                } catch (e : Error){
                    Toast.makeText(this,
                        "There is a problem from our side,\nPlease try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (name.isEmpty()) {
                    etName.error = "Name is required"
                }
                if (email.isEmpty()) {
                    etEmail.error = "Email is required"
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Enter a valid email address"
                }
                if (password.isEmpty()) {
                    etPassword.error = "Password is required"
                }
                if (password.length < 8) {
                    etPassword.error = "Password must be at least 8 characters"
                }
            }
        }
    }

    private fun exceptionHandler(exception: Exception?) {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Toast.makeText(this, "Weak password: ${exception.reason}", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(this, "Email already exists!\nElse User does not exist.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Error: ${exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}