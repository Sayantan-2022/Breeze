package com.example.breeze

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.breeze.models.LanguageViewModel
import com.example.breeze.ui.bookmarks.BookmarksFragment
import com.example.breeze.ui.home.HomeFragment
import com.example.breeze.ui.search.SearchFragment
import com.example.breeze.util.InternetChecker
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    private lateinit var database : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var sharedPreferences : SharedPreferences
    lateinit var bottomNav: ChipNavigationBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        InternetChecker().checkInternet(this, lifecycle)

        val window : Window = window
        window.statusBarColor = getColor(R.color.dark_blue)
        window.navigationBarColor = getColor(R.color.dark_blue)

        bottomNav = findViewById(R.id.bottomNav)
        sharedPreferences = getSharedPreferences("NewsAppPrefs", Context.MODE_PRIVATE)
        val savedLanguageCode = sharedPreferences.getString("preferred_language", "en") ?: "en"

        firebaseAuth = FirebaseAuth.getInstance()

        val uid = firebaseAuth.currentUser?.uid.toString()

        val btnProfile = findViewById<ShapeableImageView>(R.id.btnProfile)

        replaceFragment(HomeFragment(), uid, savedLanguageCode)
        bottomNav.setItemSelected(R.id.home, true)

        bottomNav.setOnItemSelectedListener {
            when (it) {
                R.id.home -> replaceFragment(HomeFragment(), uid, savedLanguageCode)
                R.id.search -> replaceFragment(SearchFragment(), uid, savedLanguageCode)
                R.id.bookmarks -> replaceFragment(BookmarksFragment(), uid, savedLanguageCode)
            }
        }

        database = FirebaseDatabase.getInstance().getReference("Accounts")
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
                        .into(btnProfile)
                } else if (!googlePhotoUri.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(googlePhotoUri)
                        .placeholder(R.drawable.blank_profile_picture)
                        .error(R.drawable.blank_profile_picture)
                        .into(btnProfile)
                } else {
                    btnProfile.setImageResource(R.drawable.blank_profile_picture)
                }
            }
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    fun replaceFragment(fragment: Fragment, uid : String, savedLanguageCode: String) {
        val bundle = Bundle()
        bundle.putString("uid", uid)
        bundle.putString("savedLanguageCode", savedLanguageCode)
        fragment.arguments = bundle

        val tvView = findViewById<TextView>(R.id.tvView)
        if (fragment is SearchFragment) {
            tvView.text = "Search for News"
        } else if (fragment is BookmarksFragment) {
            tvView.text = "Bookmarks"
        } else {
            tvView.text = "Breeze"
        }

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}