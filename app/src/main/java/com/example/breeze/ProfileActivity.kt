package com.example.breeze

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.breeze.models.LanguageViewModel
import com.example.breeze.util.InternetChecker
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog : Dialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var languageViewModel: LanguageViewModel

    private val languages = arrayOf("English", "Hindi", "Bengali")
    private val languageCodes = arrayOf("en", "hi", "bn")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        InternetChecker().checkInternet(this, lifecycle)

        val window : Window = window
        window.statusBarColor = getColor(R.color.dark_blue)
        window.navigationBarColor = getColor(R.color.white)

        val uid = intent.getStringExtra("uid").toString()
        firebaseAuth = FirebaseAuth.getInstance()

        val profileImage = findViewById<ShapeableImageView>(R.id.profileImage)
        val btnProfile = findViewById<ShapeableImageView>(R.id.btnProfile)
        val btnChangePic = findViewById<FloatingActionButton>(R.id.btnChangePic)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnEditName = findViewById<ImageButton>(R.id.btnEditName)
        val tvView = findViewById<TextView>(R.id.tvView)
        val languageSpinner = findViewById<Spinner>(R.id.languageSpinner)

        val stringBuilder = StringBuilder()
        Thread{
            for (letter in "Profile") {
                stringBuilder.append(letter)
                Thread.sleep(150)
                runOnUiThread {
                    tvView.text = stringBuilder.toString()
                }

            }
        }.start()

        database = FirebaseDatabase.getInstance().getReference("Accounts")
        database.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value
                val email = it.child("email").value

                tvName.text = "$name"
                tvEmail.text = "$email"
            }
        }

        database.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val imageUri = it.child("imageUri").value?.toString()
                val googlePhotoUri = it.child("googlePhotoUri").value?.toString()

                if (!imageUri.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.blank_profile_picture)
                        .error(
                            Glide.with(this)
                                .load(googlePhotoUri)
                                .placeholder(R.drawable.blank_profile_picture)
                                .error(R.drawable.blank_profile_picture)
                        )
                        .into(profileImage)

                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.blank_profile_picture)
                        .error(
                            Glide.with(this)
                                .load(googlePhotoUri)
                                .placeholder(R.drawable.blank_profile_picture)
                                .error(R.drawable.blank_profile_picture)
                        )
                        .into(btnProfile)
                } else if (!googlePhotoUri.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(googlePhotoUri)
                        .placeholder(R.drawable.blank_profile_picture)
                        .error(R.drawable.blank_profile_picture)
                        .into(profileImage)

                    Glide.with(this)
                        .load(googlePhotoUri)
                        .placeholder(R.drawable.blank_profile_picture)
                        .error(R.drawable.blank_profile_picture)
                        .into(btnProfile)
                } else {
                    profileImage.setImageResource(R.drawable.blank_profile_picture)
                    btnProfile.setImageResource(R.drawable.blank_profile_picture)
                }
            }
        }


        btnChangePic.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        sharedPreferences = getSharedPreferences("NewsAppPrefs", Context.MODE_PRIVATE)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        languageSpinner.adapter = adapter
        val savedLanguageCode = sharedPreferences.getString("preferred_language", "en") ?: "en"
        val savedPosition = languageCodes.indexOf(savedLanguageCode)
        languageSpinner.setSelection(savedPosition)

        languageViewModel = ViewModelProvider(this)[LanguageViewModel::class.java]

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguageCode = languageCodes[position]
                if (view != null) {
                    saveLanguagePreference(selectedLanguageCode, view)
                }

                languageViewModel.setLanguage(selectedLanguageCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nothing to do if not selected
            }
        }
        
        btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, WelcomeScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnEditName.setOnClickListener {
            dialog = Dialog(this)
            dialog.setContentView(R.layout.name_dialog)
            dialog.window?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.dialog_shape))
            dialog.setCancelable(false)

            dialog.show()

            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
            val btnChange = dialog.findViewById<Button>(R.id.btnChange)
            val ipNewName = dialog.findViewById<TextInputEditText>(R.id.ipNewName)

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnChange.setOnClickListener {
                val name = ipNewName.text.toString()
                val uid = intent.getStringExtra("uid").toString()

                database = FirebaseDatabase.getInstance().getReference("Accounts")
                database.child(uid).child("name").setValue(name)
                tvName.setText("Name : $name")
                dialog.dismiss()
            }
        }
    }

    private fun saveLanguagePreference(selectedLanguageCode : String, view : View) {
        val editor = sharedPreferences.edit()
        editor.putString("preferred_language", selectedLanguageCode)
        editor.apply()
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

        val uid = intent.getStringExtra("uid").toString()
        val profileImage = findViewById<ShapeableImageView>(R.id.profileImage)
        val btnProfile = findViewById<ShapeableImageView>(R.id.btnProfile)

        if(resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!

            database = FirebaseDatabase.getInstance().getReference("Accounts").child(uid)
            database.child("imageUri").setValue(uri.toString())

            profileImage.setImageURI(uri)
            btnProfile.setImageURI(uri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}