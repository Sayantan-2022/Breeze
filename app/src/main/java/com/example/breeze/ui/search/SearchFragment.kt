package com.example.breeze.ui.search

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.breeze.R
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.api.NewsAPI
import com.example.breeze.models.News
import com.example.breeze.ui.NewsWebView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.Runnable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchFragment : Fragment(R.layout.fragment_search), SwipeRefreshLayout.OnRefreshListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getString("uid").toString()
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        val etQuery = view.findViewById<TextInputEditText>(R.id.etQuery)
        val btnSearch = view.findViewById<ImageButton>(R.id.btnSearch)

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)

        val trendingNews = api.getTrendings("General", "en", "in", 1)
        loadNews(trendingNews, view, uid)

        btnSearch.setOnClickListener {
            val searchedQuery = etQuery.text.toString().trim()

            if (searchedQuery.isNotEmpty()) {
                val searchedNews = api.searchNews(searchedQuery, "en")
                loadNews(searchedNews, view, uid)
            } else {
                Snackbar.make(view, "Empty Search!", Toast.LENGTH_SHORT).show()
            }
        }

        swipeRefreshLayout.setOnRefreshListener(this@SearchFragment)
    }

    private fun loadNews(newsCall: Call<News>, view: View, uid : String) {
        newsCall.enqueue(object : Callback<News?> {
            override fun onResponse(call: Call<News?>, response: Response<News?>) {
                val responseBody = response.body()
                val dataList = responseBody?.data ?: emptyList()
                if (dataList.isNotEmpty()) {
                    val titleList = dataList.map { it.title }.toMutableList()
                    val imageUrlList = dataList.map { it.thumbnail }.toMutableList()
                    val urlList = dataList.map { it.url }.toMutableList()
                    val excerptList = dataList.map { it.excerpt }.toMutableList()

                    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, uid, this@SearchFragment)
                    recyclerView.adapter = newsAdapter

                    swipeFunction(recyclerView, newsAdapter)

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener {
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@SearchFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            startActivity(intent)
                        }
                    })
                } else {
                    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.adapter = null
                }
            }
            override fun onFailure(call: Call<News?>, t: Throwable) {
                Log.e("SearchFragment", "API call failed: ${t.message}")
            }
        })
    }

    override fun onRefresh() {
        val uid = arguments?.getString("uid").toString()

        val etQuery = view?.findViewById<TextInputEditText>(R.id.etQuery)
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)

        val searchedQuery = etQuery?.text.toString().trim()

        if (searchedQuery.isNotEmpty()) {
            val searchedNews = api.searchNews(searchedQuery, "en")
            view?.let { loadNews(searchedNews, it, uid) }
        } else {
            view?.let { Snackbar.make(it, "Empty Search!", Toast.LENGTH_SHORT).show() }
        }

        val swipeRefreshLayout = view?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        Handler().postDelayed(Runnable { swipeRefreshLayout?.isRefreshing = false }, 2000)
    }

    private fun swipeFunction(recyclerView: RecyclerView, newsAdapter: NewsAdapter) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                newsAdapter.deleteNews(viewHolder)
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
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(R.color.red)
                    .addActionIcon(R.drawable.round_delete_24)
                    .addSwipeLeftLabel("Delete")
                    .addCornerRadius(1, 20)
                    .create()
                    .decorate()

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
}