package com.example.breeze

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.breeze.ui.bookmarks.BookmarksFragment
import com.example.breeze.ui.home.HomeFragment
import com.example.breeze.ui.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        val uid = firebaseAuth.currentUser?.uid.toString()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        replaceFragment(HomeFragment(), uid)

        bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(HomeFragment(), uid)
                R.id.search -> replaceFragment(SearchFragment(), uid)
                R.id.bookmarks -> replaceFragment(BookmarksFragment(), uid)
                else -> {}
            }
            true
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