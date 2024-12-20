package com.example.breeze.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
    override fun onViewCreated(view : View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etQuery = view.findViewById<TextInputEditText>(R.id.etQuery)
        val query = etQuery.text.toString()
        val btnSearch = view.findViewById<ImageButton>(R.id.btnSearch)

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)
        val trendingNews = api.getTrendings("General", "en", "in", 2)
        trendingNews.enqueue(object : Callback<News?> {
            override fun onResponse(Call: Call<News?>, Response: Response<News?>) {
                val responseBody = Response.body()
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
                    Log.d("HomeFragment", "RecyclerView adapter attached")

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener{
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@SearchFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            startActivity(intent)
                        }
                    })
                } else {
                    Log.d("HomeFragment", "No data found in API response")
                }
            }
            override fun onFailure(Call: Call<News?>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
            }
        })

        btnSearch.setOnClickListener {
            searchForNews(query)
        }
    }

    private fun searchForNews(query: String) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)
        val searchedNews = api.searchNews(query, "en")
        searchedNews.enqueue(object : Callback<News?> {
            override fun onResponse(Call: Call<News?>, Response: Response<News?>) {
                val responseBody = Response.body()
                val dataList = responseBody?.data ?: emptyList()
                if (dataList.isNotEmpty()) {
                    val titleList = dataList.map { it.title }.toMutableList()
                    val imageUrlList = dataList.map { it.thumbnail }.toMutableList()
                    val urlList = dataList.map { it.url }.toMutableList()
                    val excerptList = dataList.map { it.excerpt }.toMutableList()

                    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView?.clearFocus()
                    recyclerView?.layoutManager = LinearLayoutManager(context)
                    val newsAdapter = NewsAdapter(titleList, imageUrlList, urlList, excerptList, this@SearchFragment)
                    recyclerView?.adapter = newsAdapter
                    Log.d("HomeFragment", "RecyclerView adapter attached")

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener{
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@SearchFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            startActivity(intent)
                        }
                    })
                } else {
                    Log.d("HomeFragment", "No data found in API response")
                }
            }
            override fun onFailure(Call: Call<News?>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
            }
        })
    }
}