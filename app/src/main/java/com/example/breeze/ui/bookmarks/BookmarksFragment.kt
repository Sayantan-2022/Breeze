package com.example.breeze.ui.bookmarks

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.breeze.MainActivity
import com.example.breeze.R
import com.example.breeze.adapter.BookmarkListener
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.models.News
import com.example.breeze.ui.NewsWebView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.Runnable
import retrofit2.Call

class BookmarksFragment : Fragment(R.layout.fragment_bookmarks),
    SwipeRefreshLayout.OnRefreshListener,
        BookmarkListener
{

    lateinit var database : DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar3)
        progressBar.visibility = View.VISIBLE

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
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar3)
        progressBar.visibility = View.GONE

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, uid, this@BookmarksFragment, this)
        recyclerView.adapter = newsAdapter

        swipeFunction(recyclerView, newsAdapter, titleList)

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

    private fun swipeFunction(recyclerView: RecyclerView, newsAdapter: NewsAdapter, titleList: MutableList<String>) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val uid = arguments?.getString("uid") ?: return // Exit if uid is null
                val position = viewHolder.absoluteAdapterPosition

                if (position < 0 || position >= titleList.size) {
                    Snackbar.make(view ?: return, "Invalid item position!", Snackbar.LENGTH_SHORT).show()
                    return
                }
                // Access the database
                database = FirebaseDatabase.getInstance().getReference("Bookmarks")

                database.child(uid).get().addOnSuccessListener {
                    if(it.exists()){
                        for (valueChild in it.children) {
                            if (valueChild.child("title").value.toString() == titleList[position]) {
                                valueChild.ref.removeValue()
                                viewHolder.itemView.findViewById<ImageButton>(R.id.btnBookmark).setImageResource(R.drawable.baseline_bookmark_border_24)

                                newsAdapter.bookmarkListener?.onBookmarkRemoved(viewHolder.absoluteAdapterPosition)
                                break
                            }
                        }
                    }
                }

                // Show a Snackbar with an Undo option
                view?.let {
                    Snackbar.make(it, "Bookmark Deleted!", Snackbar.LENGTH_SHORT)
                        .setAction("Undo") {
                            // Add undo logic if needed
                        }.show()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                this@BookmarksFragment.context?.let { ContextCompat.getColor(it, R.color.red)}?.let {
                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(it)
                        .addActionIcon(R.drawable.round_delete_24)
                        .addSwipeLeftLabel("Delete")
                        .setSwipeLeftLabelTextSize(1, 16F)
                        .setSwipeLeftLabelColor(ContextCompat.getColor(this@BookmarksFragment.requireContext(), R.color.white))
                        .addCornerRadius(1, 20)
                        .create()
                        .decorate()
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onBookmarkRemoved(position: Int) {
        database = FirebaseDatabase.getInstance().getReference("Bookmarks")
        val uid = arguments?.getString("uid").toString()

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = recyclerView?.adapter as? NewsAdapter

        adapter?.let {
            it.titleList.removeAt(position)
            it.imageUrlList.removeAt(position)
            it.excerptList.removeAt(position)
            it.urlList.removeAt(position)
            it.notifyItemRemoved(position)
            it.notifyItemRangeChanged(position, it.itemCount)
        }

        Snackbar.make(requireView(), "Bookmark removed!", Snackbar.LENGTH_SHORT).show()

        database.child(uid).get().addOnSuccessListener {
            if (!it.exists()) {
                val mainActivity = activity as MainActivity
                mainActivity.replaceFragment(NoBookmarkFragment(), uid)
            }
        }
    }
}