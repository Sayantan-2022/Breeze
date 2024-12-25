package com.example.breeze.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.breeze.R
import com.example.breeze.models.Bookmark
import com.example.breeze.ui.bookmarks.BookmarksFragment
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

lateinit var database : DatabaseReference

class NewsAdapter(var titleList: MutableList<String>,
                  var imageUrlList: MutableList<String>,
                  var excerptList: MutableList<String>,
                  val urlList: MutableList<String>,
                  val uid : String,
                  val context: Fragment,
                  private val bookmarkListener: BookmarkListener? = null)
    : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private lateinit var newsListener: onCardClickListener

    interface onCardClickListener{
        fun onCardClick(position: Int)
    }

    fun setOnCardClickListener(listener: onCardClickListener){
        newsListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.default_card, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        database = FirebaseDatabase.getInstance().getReference("Bookmarks")

        holder.title.text=titleList[position]
        Glide.with(context)
            .load(imageUrlList[position])
            .fitCenter()
            .placeholder(R.drawable.baseline_downloading_24)
            .error(R.drawable.baseline_error_outline_24)
            .into(holder.image)
        holder.excerpt.text=excerptList[position]

        holder.itemView.setOnClickListener{
            newsListener.onCardClick(position)
        }

        database.child(uid).get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                if (child.child("title").value.toString() == titleList[position]) {
                    holder.btnBookmark.setImageResource(R.drawable.baseline_bookmark_remove_24)
                }
            }
        }.addOnFailureListener {
            Log.e("NewsAdapter", "Failed to fetch bookmarks: ${it.message}")
        }

        holder.btnBookmark.setOnClickListener {
            isBookmarked(uid, titleList[position]) { bookmarked ->
                if (bookmarked) {
                    database.child(uid).get().addOnSuccessListener {
                        if(it.exists()){
                            for (valueChild in it.children) {
                                if (valueChild.child("title").value.toString() == titleList[position]) {
                                    valueChild.ref.removeValue()
                                    break
                                }
                            }
                        }
                    }
                    holder.btnBookmark.setImageResource(R.drawable.baseline_bookmark_border_24)

                    bookmarkListener?.onBookmarkRemoved(holder.absoluteAdapterPosition)
                } else {
                    val bookmark = Bookmark(
                        titleList[position],
                        imageUrlList[position],
                        excerptList[position],
                        urlList[position]
                    )

                    val key = database.child(uid).push().key

                    bookmark.key = key
                    if (key != null) {
                        database.child(uid).child(key).setValue(bookmark)
                    }
                    holder.btnBookmark.setImageResource(R.drawable.baseline_bookmark_remove_24)
                }
            }
        }
    }

    private fun isBookmarked(uid : String, title : String, callback: (Boolean) -> Unit) {
        database.child(uid).get().addOnSuccessListener { snapshot ->
            var bookmarked = false
            for (child in snapshot.children) {
                if (child.child("title").value.toString() == title) {
                    bookmarked = true
                    break
                }
            }
            callback(bookmarked)
        }.addOnFailureListener {
            callback(false)
        }
    }

    override fun getItemCount(): Int {
        return titleList.size
    }

    fun deleteNews(viewHolder: RecyclerView.ViewHolder) {
        titleList.removeAt(viewHolder.adapterPosition)
        imageUrlList.removeAt(viewHolder.adapterPosition)
        excerptList.removeAt(viewHolder.adapterPosition)
        urlList.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)
    }

    fun addBookmark(viewHolder: RecyclerView.ViewHolder, view: View) {
        isBookmarked(uid, titleList[viewHolder.adapterPosition]) { bookmarked ->
            if(!bookmarked) {
                val bookmark = Bookmark(
                    titleList[viewHolder.adapterPosition],
                    imageUrlList[viewHolder.adapterPosition],
                    excerptList[viewHolder.adapterPosition],
                    urlList[viewHolder.adapterPosition]
                )

                val title = if(titleList[viewHolder.adapterPosition].contains('.')) {
                    titleList[viewHolder.adapterPosition].substring(0, titleList[viewHolder.adapterPosition].indexOf('.'))
                } else {
                    titleList[viewHolder.adapterPosition]
                }
                database.child(uid).child(title).setValue(bookmark)
                Snackbar.make(view, "Bookamark added!", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(view, "Bookamark already added!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tvHeading)
        val image = itemView.findViewById<ShapeableImageView>(R.id.headingImage)
        val excerpt = itemView.findViewById<TextView>(R.id.tvExcerpt)
        val btnBookmark = itemView.findViewById<ImageButton>(R.id.btnBookmark)
    }
}