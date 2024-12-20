package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ForgotPassword : AppCompatActivity() {

    private lateinit var database : DatabaseReference
    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val etUsername = findViewById<TextInputEditText>(R.id.ipUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.ipEmail)
        val etNewPassword = findViewById<TextInputEditText>(R.id.ipNewPassword)
        val etRePassword = findViewById<TextInputEditText>(R.id.ipRePassword)

        btnChangePassword.setOnClickListener {
            val username = etUsername.text?.toString()
            val email = etEmail.text?.toString()
            val newPassword = etNewPassword.text?.toString()
            val rePassword = etRePassword.text?.toString()

            if (username != null && email != null && newPassword != null && rePassword != null) {
                if (newPassword == rePassword) {
                    updateData(username, newPassword)
                } else {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateData(username : String?, newPassword: String?){
        database = FirebaseDatabase.getInstance().getReference("Accounts")
        if(username != null) {
            database.child(username).get().addOnSuccessListener {
                if (it.exists()) {
                    val email = it.child("email").value.toString()
                    val name = it.child("name").value.toString()

                    val map = mapOf("email" to email, "name" to name, "password" to newPassword, "username" to username)
                    database.child(username).updateChildren(map)

                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, SignIn::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "User does not exist!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch data!", Toast.LENGTH_SHORT).show()
            }
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