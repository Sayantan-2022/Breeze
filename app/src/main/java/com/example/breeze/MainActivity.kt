package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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

    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        InternetChecker().checkInternet(this, lifecycle)

        val window : Window = window
        window.statusBarColor = getColor(R.color.dark_blue)
        window.navigationBarColor = getColor(R.color.dark_blue)

        firebaseAuth = FirebaseAuth.getInstance()

        val uid = firebaseAuth.currentUser?.uid.toString()

        val bottomNav = findViewById<ChipNavigationBar>(R.id.bottomNav)
        val btnProfile = findViewById<ShapeableImageView>(R.id.btnProfile)

        replaceFragment(HomeFragment(), uid)

        bottomNav.setOnItemSelectedListener {
            when (it) {
                R.id.home -> replaceFragment(HomeFragment(), uid)
                R.id.search -> replaceFragment(SearchFragment(), uid)
                R.id.bookmarks -> replaceFragment(BookmarksFragment(), uid)
            }
        }

        database = FirebaseDatabase.getInstance().getReference("Accounts")
        database.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val imageUri = it.child("imageUri").value
                if (imageUri != null && imageUri.toString().isNotEmpty())
                    Glide.with(this)
                        .load(imageUri.toString())
                        .placeholder(R.drawable.blank_profile_picture)
                        .error(R.drawable.blank_profile_picture)
                        .into(btnProfile)
                else
                    btnProfile.setImageResource(R.drawable.blank_profile_picture)
            }
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    fun replaceFragment(fragment: Fragment, uid : String) {
        val bundle = Bundle()
        bundle.putString("uid", uid)
        fragment.arguments = bundle

        val tvView1 = findViewById<TextView>(R.id.tvView1)
        val tvView2 = findViewById<TextView>(R.id.tvView2)
        val stringBuilder = StringBuilder()
        Thread.sleep(10)
        if (fragment is SearchFragment) {
            Thread{
                tvView1.visibility = View.GONE
                tvView2.visibility = View.VISIBLE
                for (letter in "Search for News") {
                    stringBuilder.append(letter)
                    Thread.sleep(100)
                    runOnUiThread {
                        tvView2.text = stringBuilder.toString()
                    }

                }
            }.start()
        }else if (fragment is BookmarksFragment) {
            tvView1.visibility = View.VISIBLE
            tvView2.visibility = View.GONE
            tvView1.text = "Bookmarks"
        }
        else {
            tvView1.visibility = View.VISIBLE
            tvView2.visibility = View.GONE
            tvView1.text = "Breeze"
        }

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}