package com.example.breeze

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.breeze.ui.bookmarks.BookmarksFragment
import com.example.breeze.ui.home.HomeFragment
import com.example.breeze.ui.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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
        }
    }

    fun replaceFragment(fragment: Fragment, uid : String) {
        val bundle = Bundle()
        bundle.putString("uid", uid)
        fragment.arguments = bundle

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}