package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.breeze.util.Account
import com.example.breeze.util.InternetChecker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    
    private lateinit var database : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        InternetChecker().checkInternet(this, lifecycle)

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
                            }
                        }
                } catch (e : Error){
                    Toast.makeText(this,
                        "There is a problem from our side,\nPlease try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else Toast.makeText(this, "Please all fields!", Toast.LENGTH_SHORT).show()
        }
    }
}