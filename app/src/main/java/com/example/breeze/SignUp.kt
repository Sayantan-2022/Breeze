package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    
    private lateinit var database: DatabaseReference
    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)

        val etName = findViewById<TextInputEditText>(R.id.ipName)
        val etUsername = findViewById<TextInputEditText>(R.id.ipUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.ipEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.ipPassword)

        btnSignUp.setOnClickListener {
            val name = etName.text.toString()
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            val account = Account(name, username, email, password)

            database = FirebaseDatabase.getInstance().getReference("Accounts")
            database.child(username).get().addOnSuccessListener {
                if(it.exists()){
                    Toast.makeText(this, "User already exists!", Toast.LENGTH_LONG).show()
                } else {
                    if (name.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                        try {
                            database.child(username).setValue(account).addOnSuccessListener {
                                etName.text?.clear()
                                etUsername.text?.clear()
                                etEmail.text?.clear()
                                etPassword.text?.clear()

                                Toast.makeText(this, "User Registered!", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("username", username)
                                startActivity(intent)
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "There is a problem from our side,\nPlease try again!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e : Error){
                            Toast.makeText(this,
                                "Username should not contain:\nSpaces, Special Characters except _",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else Toast.makeText(this, "Please all fields!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch Data", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignIn.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}