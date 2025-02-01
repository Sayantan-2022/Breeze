package com.example.breeze

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.breeze.util.InternetChecker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import okhttp3.internal.concurrent.Task

class SignIn : AppCompatActivity() {

    private var firebaseAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Accounts")
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val activityResultLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
                result ->
            if(result.resultCode == RESULT_OK){
                val accountTask : com.google.android.gms.tasks.Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val signInAccount : GoogleSignInAccount = accountTask.result
                    val authCredential : AuthCredential = GoogleAuthProvider.getCredential(signInAccount.idToken, null)

                    firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener {
                        if(it.isSuccessful){
                            val user = firebaseAuth.currentUser
                            if(user != null){
                                firebaseAuth.currentUser?.reload()?.addOnCompleteListener { reloadTask ->
                                    if (reloadTask.isSuccessful) {
                                        Log.d("AuthDebug", "User reloaded successfully")
                                        val userMail = firebaseAuth.currentUser?.email
                                        Log.d("AuthDebug", "User Email after reload: $userMail")

                                        if (userMail != null) {
                                            checkUserInDatabase(userMail, user)
                                        } else {
                                            Log.e("AuthDebug", "User email still NULL after reload")
                                        }
                                    } else {
                                        Log.e("AuthDebug", "User reload failed", reloadTask.exception)
                                    }
                                }
                            } else {
                                Log.e("AuthDebug", "Sign-in successful, but user is null")
                            }
                        } else {
                            Log.e("AuthDebug", "Firebase Sign-In failed", it.exception)
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "There is a problem from our side,\nPLease try again later!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e : Exception){
                    Toast.makeText(this, "There is a problem from our side,\nPLease try again later!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        InternetChecker().checkInternet(this, lifecycle)

        val window : Window = window
        window.statusBarColor = getColor(R.color.dark_blue)
        window.navigationBarColor = getColor(R.color.orange)

        val gSignIn = findViewById<SignInButton>(R.id.gSignIn)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val etEmail = findViewById<TextInputEditText>(R.id.ipEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.ipPassword)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 8) {
                readData(email, password)
            } else {
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

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        gSignIn.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.G_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, options)

            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    val intent = googleSignInClient.signInIntent
                    activityResultLauncher.launch(intent)
                }
            }
        }
    }

    private fun readData(email: String, password: String) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this, "Sign In Successful!", Toast.LENGTH_SHORT).show()

                    val uid = it.result.user?.uid.toString()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("uid", uid)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                } else {
                    exceptionHandler(it.exception)
                }
            }.addOnFailureListener {
                Toast.makeText(this,
                    "There is a problem from our side,\nPlease try again!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun exceptionHandler(exception: Exception?) {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Toast.makeText(this, "Weak password: ${exception.reason}", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(this, "This email is already registered. Try another sign-in method.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Error: ${exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserInDatabase(userMail: String, user : FirebaseUser) {
        Log.d("AuthDebug", "Checking user in database for email: $userMail")

        database = FirebaseDatabase.getInstance().getReference("Accounts")
        database.get()
            .addOnSuccessListener { snapshot ->
                for (uidChild in snapshot.children ) {
                    Log.d("UidChild: ", uidChild.key.toString())
                    if (uidChild.child("email").value.toString() == userMail) {
                        val uid = uidChild.key.toString()
                        Log.d("AuthDebug", "User found in database. UID: $uid")
                        database.child(uidChild.key.toString()).child("googlePhotoUri").setValue(user.photoUrl.toString())

                        navigateToMainActivity(uid)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AuthDebug", "Database lookup failed", e)
            }
    }


    private fun navigateToMainActivity(uid: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("uid", uid)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}