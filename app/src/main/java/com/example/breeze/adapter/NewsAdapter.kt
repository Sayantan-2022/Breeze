package com.example.breeze.adapter

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
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

private lateinit var database : DatabaseReference

class NewsAdapter(var titleList: MutableList<String>,
                  var imageUrlList: MutableList<String>,
                  var excerptList: MutableList<String>,
                  val urlList: MutableList<String>,
                  val uid : String,
                  val context: Fragment)
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

        var title = titleList[position]
        if(title.contains('.')) {
            title = title.substring(0, title.indexOf('.'))
        }

        isBookmarked(uid, title) { bookmarked ->
            holder.btnBookmark.setImageResource(
                if (bookmarked) R.drawable.baseline_bookmark_remove_24
                else R.drawable.baseline_bookmark_border_24
            )
        }

        holder.btnBookmark.setOnClickListener {
            isBookmarked(uid, titleList[position]) { bookmarked ->
                if (bookmarked) {
                    database.child(uid).child(title).removeValue()
                    holder.btnBookmark.setImageResource(R.drawable.baseline_bookmark_border_24)
                } else {
                    val bookmark = Bookmark(
                        titleList[position],
                        imageUrlList[position],
                        excerptList[position],
                        urlList[position]
                    )
                    database.child(uid).child(title).setValue(bookmark)
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

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tvHeading)
        val image = itemView.findViewById<ShapeableImageView>(R.id.headingImage)
        val excerpt = itemView.findViewById<TextView>(R.id.tvExcerpt)
        val btnBookmark = itemView.findViewById<ImageButton>(R.id.btnBookmark)
    }
}