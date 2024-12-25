package com.example.breeze

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private  lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val uid = intent.getStringExtra("uid").toString()
        firebaseAuth = FirebaseAuth.getInstance()

        val profileImage = findViewById<ShapeableImageView>(R.id.profileImage)
        val btnProfile = findViewById<ShapeableImageView>(R.id.btnProfile)
        val btnChangePic = findViewById<FloatingActionButton>(R.id.btnChangePic)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnEditName = findViewById<ImageButton>(R.id.btnEditName)

        database = FirebaseDatabase.getInstance().getReference("Accounts")
        database.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value
                val email = it.child("email").value

                tvName.setText("Name : $name")
                tvEmail.setText("Email : $email")
            }
        }

        btnChangePic.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, SignUp::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    @Deprecated(
        "This method has been deprecated in favor of using the Activity Result API\n" +
                "which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n" +
                "contracts for common intents available in\n" +
                "{@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n" +
                "testing, and allow receiving results in separate, testable classes independent from your\n" +
                "activity. Use\n{@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n" +
                "with the appropriate {@link ActivityResultContract} and handling the result in the\n" +
                "{@link ActivityResultCallback#onActivityResult(Object) callback}."
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val profileImage = findViewById<ShapeableImageView>(R.id.profileImage)
        val btnProfile = findViewById<ShapeableImageView>(R.id.btnProfile)

        if(resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!
            profileImage.setImageURI(uri)
            btnProfile.setImageURI(uri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}