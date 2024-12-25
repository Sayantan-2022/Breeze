package com.example.breeze.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.breeze.R
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.api.NewsAPI
import com.example.breeze.models.News
import com.example.breeze.ui.NewsWebView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random


class HomeFragment : Fragment(R.layout.fragment_home), SwipeRefreshLayout.OnRefreshListener{

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view : View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getString("uid").toString()
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)
        val trendingNews = api.getTrendings("General", "en", "in", 2)

        trendingNews.enqueue(object : Callback<News?> {
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
                    val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, uid, this@HomeFragment)
                    recyclerView.adapter = newsAdapter

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener{
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@HomeFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            startActivity(intent)
                        }
                    })
                } else {
                    Log.d("HomeFragment", "No data found in API response")
                }
            }
            override fun onFailure(call: Call<News?>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
            }
        })

        swipeRefreshLayout.setOnRefreshListener(this@HomeFragment::onRefresh)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onRefresh() {
        val uid = arguments?.getString("uid").toString()

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val pageNumber = Random.nextInt(1, 20)
        val api = retrofitBuilder.create(NewsAPI::class.java)
        val trendingNews = api.getTrendings("General", "en", "in", pageNumber)

        trendingNews.enqueue(object : Callback<News?> {
            override fun onResponse(call: Call<News?>, response: Response<News?>) {
                val responseBody = response.body()

                val dataList = responseBody?.data ?: emptyList()

                if (dataList.isNotEmpty()) {
                    val titleList = dataList.map { it.title }.toMutableList()
                    val imageUrlList = dataList.map { it.thumbnail }.toMutableList()
                    val urlList = dataList.map { it.url }.toMutableList()
                    val excerptList = dataList.map { it.excerpt }.toMutableList()

                    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView?.layoutManager = LinearLayoutManager(context)
                    val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, uid, this@HomeFragment)
                    recyclerView?.adapter = newsAdapter

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener{
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@HomeFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            startActivity(intent)
                        }
                    })
                } else {
                    Log.d("HomeFragment", "No data found in API response")
                }
            }

            override fun onFailure(call: Call<News?>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
            }
        })

        val swipeRefreshLayout = view?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        Handler(Looper.getMainLooper()).postDelayed({
            swipeRefreshLayout?.isRefreshing = false
        }, 3000)
    }
}