package com.example.breeze.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.breeze.R
import com.google.android.material.imageview.ShapeableImageView

class NewsAdapter(var titleList: MutableList<String>,
                  var imageUrlList: MutableList<String>,
                  var urlList: MutableList<String>,
                  var excerptList: MutableList<String>,
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
        return NewsViewHolder(itemView, newsListener)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.title.text=titleList[position]
        Glide.with(context)
            .load(imageUrlList[position])
            .fitCenter()
            .placeholder(R.drawable.baseline_downloading_24)
            .error(R.drawable.baseline_error_outline_24)
            .into(holder.image)
        holder.excerpt.text=excerptList[position]
    }

    override fun getItemCount(): Int {
        return titleList.size
    }

    class NewsViewHolder(itemView: View, listener: onCardClickListener) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tvHeading)
        val image = itemView.findViewById<ShapeableImageView>(R.id.headingImage)
        val excerpt = itemView.findViewById<TextView>(R.id.tvExcerpt)

        init {
            listener.onCardClick(adapterPosition)
        }
    }
}