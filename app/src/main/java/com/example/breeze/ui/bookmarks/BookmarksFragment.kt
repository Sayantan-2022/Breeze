package com.example.breeze.ui.bookmarks

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.breeze.MainActivity
import com.example.breeze.R
import com.example.breeze.adapter.BookmarkListener
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.ui.NewsWebView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.Runnable

class BookmarksFragment : Fragment(R.layout.fragment_bookmarks),
    SwipeRefreshLayout.OnRefreshListener,
        BookmarkListener
{

    lateinit var database : DatabaseReference
    private var bottomNav: ChipNavigationBar? = (activity as? MainActivity)?.bottomNav

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retainInstance = true

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar3)
        progressBar.visibility = View.VISIBLE

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val uid = arguments?.getString("uid").toString()
        val savedLanguageCode = arguments?.getString("savedLanguageCode").toString()

        val titleList = mutableListOf<String>()
        val imageUrlList = mutableListOf<String>()
        val excerptList = mutableListOf<String>()
        val urlList = mutableListOf<String>()
        val publisherName = mutableListOf<String>()
        val publisherIcon = mutableListOf<String>()

        database = FirebaseDatabase.getInstance().getReference("Bookmarks")

        database.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                for (child in it.children){
                    titleList.add(child.child("title").value.toString())
                    imageUrlList.add(child.child("imageUrl").value.toString())
                    excerptList.add(child.child("excerpt").value.toString())
                    urlList.add(child.child("url").value.toString())
                    publisherName.add(child.child("publisherName").value.toString())
                    publisherIcon.add(child.child("publisherIcon").value.toString())
                }

                loadNews(titleList, imageUrlList, excerptList, urlList, publisherName, publisherIcon, view, uid)
            } else {
                (activity as MainActivity).replaceFragment(NoBookmarkFragment(), uid, savedLanguageCode)
                val snackbarNoBookmark = Snackbar.make(view, "No Bookmarks found!", Snackbar.LENGTH_SHORT)
                snackbarNoBookmark.anchorView = requireActivity().findViewById(R.id.bottomNav)

                val params = snackbarNoBookmark.view.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = resources.getDimensionPixelSize(R.dimen.snackbar_margin)
                snackbarNoBookmark.view.layoutParams = params

                snackbarNoBookmark.show()
            }
        }.addOnFailureListener {
            Toast.makeText(this@BookmarksFragment.context, "Failed to fetch bookmarks!", Toast.LENGTH_SHORT).show()
        }

        swipeRefreshLayout.setOnRefreshListener(this@BookmarksFragment)
    }

    private fun loadNews(titleList : MutableList<String>,
                         imageUrlList : MutableList<String>,
                         excerptList : MutableList<String>,
                         urlList : MutableList<String>,
                         publisherName : MutableList<String>,
                         publisherIcon : MutableList<String>,
                         view : View,
                         uid : String) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar3)
        progressBar.visibility = View.GONE

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, publisherName, publisherIcon, uid, this@BookmarksFragment, this)
        recyclerView.adapter = newsAdapter

        swipeFunction(recyclerView, newsAdapter, titleList)

        newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener {
            override fun onCardClick(position: Int) {
                val intent = Intent(this@BookmarksFragment.context, NewsWebView::class.java)
                intent.putExtra("url", urlList[position])
                intent.putExtra("uid", uid)
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
        val publisherName = mutableListOf<String>()
        val publisherIcon = mutableListOf<String>()

        database = FirebaseDatabase.getInstance().getReference("Bookmarks")

        database.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                for (child in it.children){
                    titleList.add(child.child("title").value.toString())
                    imageUrlList.add(child.child("imageUrl").value.toString())
                    excerptList.add(child.child("excerpt").value.toString())
                    urlList.add(child.child("url").value.toString())
                    publisherName.add(child.child("publisherName").value.toString())
                    publisherIcon.add(child.child("publisherIcon").value.toString())
                }

                view?.let { it1 ->
                    loadNews(titleList, imageUrlList, excerptList, urlList, publisherName, publisherIcon,
                        it1, uid)
                }
            } else {
                (activity as MainActivity).replaceFragment(NoBookmarkFragment(), uid, savedLanguageCode = "en")
                view?.let { it1 -> val snackbarNoBookmark = Snackbar.make(it1, "No Bookmarks found!", Snackbar.LENGTH_SHORT)
                    snackbarNoBookmark.anchorView = requireActivity().findViewById(R.id.bottomNav)

                    val params = snackbarNoBookmark.view.layoutParams as ViewGroup.MarginLayoutParams
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.snackbar_margin)
                    snackbarNoBookmark.view.layoutParams = params

                    snackbarNoBookmark.show() }
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
                var removedKey: String? = null

                if (position < 0 || position >= titleList.size) {
                    Snackbar.make(view ?: return, "Invalid item position!", Snackbar.LENGTH_SHORT).setAnchorView(bottomNav).show()
                    return
                }
                // Access the database
                database = FirebaseDatabase.getInstance().getReference("Bookmarks")

                database.child(uid).get().addOnSuccessListener {
                    if(it.exists()){
                        for (valueChild in it.children) {
                            if (valueChild.child("title").value.toString() == titleList[position]) {
                                removedKey = valueChild.key
                                valueChild.ref.removeValue()
                                viewHolder.itemView.findViewById<ImageButton>(R.id.btnBookmark).setImageResource(R.drawable.baseline_bookmark_border_24)

                                newsAdapter.bookmarkListener?.onBookmarkRemoved(viewHolder.absoluteAdapterPosition, removedKey)
                                break
                            }
                        }
                    }
                }

                database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.hasChildren()) {
                            if (isAdded && activity != null) {
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.frameLayout, NoBookmarkFragment())
                                    .commit()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
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
                        .addSwipeLeftLabel("Remove")
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

    override fun onBookmarkRemoved(position: Int, removedKey: String?) {
        database = FirebaseDatabase.getInstance().getReference("Bookmarks")
        val uid = arguments?.getString("uid").toString()

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = recyclerView?.adapter as? NewsAdapter

        val removedItem = mapOf(
            "title" to (adapter?.titleList?.get(position) ?: ""),
            "imageUrl" to (adapter?.imageUrlList?.get(position) ?: ""),
            "excerpt" to (adapter?.excerptList?.get(position) ?: ""),
            "url" to (adapter?.urlList?.get(position) ?: ""),
            "publisherName" to (adapter?.publisherName?.get(position) ?: ""),
            "publisherIcon" to (adapter?.publisherIcon?.get(position) ?: "")
        )

        adapter?.let {
            it.titleList.removeAt(position)
            it.imageUrlList.removeAt(position)
            it.excerptList.removeAt(position)
            it.urlList.removeAt(position)
            it.notifyItemRemoved(position)
            it.notifyItemRangeChanged(position, it.itemCount)
        }

        val snackbarRemoved = Snackbar.make(requireView(), "Bookmark removed!", Snackbar.LENGTH_SHORT)
        snackbarRemoved.anchorView = requireActivity().findViewById(R.id.bottomNav)
        snackbarRemoved.setAction("Undo"){
            if (removedKey != null) {
                val dbRef = database.child(uid).child(removedKey)
                dbRef.setValue(removedItem).addOnSuccessListener {
                    adapter?.titleList?.add(position, removedItem["title"].toString())
                    adapter?.imageUrlList?.add(position, removedItem["imageUrl"].toString())
                    adapter?.excerptList?.add(position, removedItem["excerpt"].toString())
                    adapter?.urlList?.add(position, removedItem["url"].toString())
                    adapter?.publisherName?.add(position, removedItem["publisherName"].toString())
                    adapter?.publisherIcon?.add(position, removedItem["publisherIcon"].toString())

                    adapter?.notifyItemInserted(position)
                    adapter?.notifyItemRangeChanged(position, adapter.itemCount)

                    if(isAdded) {
                        val navController = findNavController()
                        navController.navigate(R.id.action_noBookmarksFragment_to_bookmarksFragment)
                    }
                }
            }
        }

        val params = snackbarRemoved.view.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = resources.getDimensionPixelSize(R.dimen.snackbar_margin)
        snackbarRemoved.view.layoutParams = params

        snackbarRemoved.show()

        checkBookmarksAndUpdateFragment(uid)
    }

    private fun checkBookmarksAndUpdateFragment(uid: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Bookmarks").child(uid)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    if (isAdded) {
                        requireActivity().supportFragmentManager.beginTransaction()
                            .detach(this@BookmarksFragment)
                            .attach(this@BookmarksFragment)
                            .commitAllowingStateLoss()
                    }
                } else {
                    if (isAdded) {
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, NoBookmarkFragment())
                            .commitAllowingStateLoss()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching bookmarks: ${error.message}")
            }
        })
    }
}