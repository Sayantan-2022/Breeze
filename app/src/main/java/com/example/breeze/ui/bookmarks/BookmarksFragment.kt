package com.example.breeze.ui.bookmarks

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.breeze.MainActivity
import com.example.breeze.R
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.models.News
import com.example.breeze.ui.NewsWebView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Runnable
import retrofit2.Call

class BookmarksFragment : Fragment(R.layout.fragment_bookmarks), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var database : DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val uid = arguments?.getString("uid").toString()

        val titleList = mutableListOf<String>()
        val imageUrlList = mutableListOf<String>()
        val excerptList = mutableListOf<String>()
        val urlList = mutableListOf<String>()

        database = FirebaseDatabase.getInstance().getReference("Bookmarks")

        database.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                for (child in it.children){
                    titleList.add(child.child("title").value.toString())
                    imageUrlList.add(child.child("imageUrl").value.toString())
                    excerptList.add(child.child("excerpt").value.toString())
                    urlList.add(child.child("url").value.toString())
                }

                loadNews(titleList, imageUrlList, excerptList, urlList, view, uid)
            } else {
                (activity as MainActivity).replaceFragment(NoBookmarkFragment(), uid)
                Snackbar.make(view, "No Bookmarks found!", Snackbar.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Snackbar.make(view, "Failed to fetch bookmarks!", Snackbar.LENGTH_SHORT).show()
        }

        swipeRefreshLayout.setOnRefreshListener(this@BookmarksFragment)
    }

    private fun loadNews(titleList : MutableList<String>,
                         imageUrlList : MutableList<String>,
                         excerptList : MutableList<String>,
                         urlList : MutableList<String>,
                         view : View,
                         uid : String) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, uid, this@BookmarksFragment)
        recyclerView.adapter = newsAdapter

        newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener {
            override fun onCardClick(position: Int) {
                val intent = Intent(this@BookmarksFragment.context, NewsWebView::class.java)
                intent.putExtra("url", urlList[position])
                startActivity(intent)
            }
        })
    }

    override fun onRefresh() {
        val uid = arguments?.getString("uid").toString()

        val titleList = mutableListOf<String>()
        val imageUrlList = mutableListOf<String>()
        val excerptList = mutableListOf<String>()
        val urlList = mutableListOf<String>()

        database = FirebaseDatabase.getInstance().getReference("Bookmarks")

        database.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                for (child in it.children){
                    titleList.add(child.child("title").value.toString())
                    imageUrlList.add(child.child("imageUrl").value.toString())
                    excerptList.add(child.child("excerpt").value.toString())
                    urlList.add(child.child("url").value.toString())
                }

                view?.let { it1 ->
                    loadNews(titleList, imageUrlList, excerptList, urlList,
                        it1, uid)
                }
            } else {
                (activity as MainActivity).replaceFragment(NoBookmarkFragment(), uid)
                view?.let { it1 -> Snackbar.make(it1, "No Bookmarks found!", Snackbar.LENGTH_SHORT).show() }
            }
        }.addOnFailureListener {
            Toast.makeText(this@BookmarksFragment.context, "Failed to fetch bookmarks!", Toast.LENGTH_SHORT).show()
        }

        val swipeRefreshLayout = view?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        Handler().postDelayed(Runnable {
            swipeRefreshLayout?.isRefreshing = false
        }, 1000)
    }
}