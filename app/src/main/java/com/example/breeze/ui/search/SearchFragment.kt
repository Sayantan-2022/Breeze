package com.example.breeze.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.breeze.R
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.api.NewsAPI
import com.example.breeze.models.News
import com.example.breeze.ui.NewsWebView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchFragment : Fragment(R.layout.fragment_search) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etQuery = view.findViewById<TextInputEditText>(R.id.etQuery)
        val btnSearch = view.findViewById<ImageButton>(R.id.btnSearch)

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)

        // Load trending news initially
        val trendingNews = api.getTrendings("General", "en", "in", 1)
        loadNews(trendingNews, view)

        // Handle search button click
        btnSearch.setOnClickListener {
            val query = etQuery.text.toString().trim()
            Log.d("SearchFragment", "Search button clicked with query: $query")// Get the latest query from the input field
            if (query.isNotEmpty()) {
                val searchedNews = api.searchNews(query, "en")
                loadNews(searchedNews, view)
            } else {
                Toast.makeText(this@SearchFragment.context, "Empty Search!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadNews(newsCall: Call<News>, view: View) {
        newsCall.enqueue(object : Callback<News?> {
            override fun onResponse(call: Call<News?>, response: Response<News?>) {
                val responseBody = response.body()
                Log.d("SearchFragment", "Response Body: $responseBody")
                val dataList = responseBody?.data ?: emptyList()
                if (dataList.isNotEmpty()) {
                    val titleList = dataList.map { it.title }.toMutableList()
                    val imageUrlList = dataList.map { it.thumbnail }.toMutableList()
                    val urlList = dataList.map { it.url }.toMutableList()
                    val excerptList = dataList.map { it.excerpt }.toMutableList()

                    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    val newsAdapter = NewsAdapter(titleList, imageUrlList, urlList, excerptList, this@SearchFragment)
                    recyclerView.adapter = newsAdapter
                    Log.d("SearchFragment", "RecyclerView adapter updated with new data")

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener {
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@SearchFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            startActivity(intent)
                        }
                    })
                } else {
                    Log.d("SearchFragment", "No data found for the query")
                    // Clear the RecyclerView if no data is found
                    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.adapter = null
                }
            }
            override fun onFailure(call: Call<News?>, t: Throwable) {
                Log.e("SearchFragment", "API call failed: ${t.message}")
            }
        })
    }
}