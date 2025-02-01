package com.example.breeze.ui.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.breeze.MainActivity
import com.example.breeze.R
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.api.NewsAPI
import com.example.breeze.models.LanguageViewModel
import com.example.breeze.models.News
import com.example.breeze.models.Publisher
import com.example.breeze.ui.NewsWebView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random


class HomeFragment : Fragment(R.layout.fragment_home), SwipeRefreshLayout.OnRefreshListener{

    private lateinit var languageViewModel: LanguageViewModel
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view : View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        val uid = arguments?.getString("uid").toString()
        val savedLanguageCode = arguments?.getString("savedLanguageCode").toString()
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)

        languageViewModel = ViewModelProvider(requireActivity())[LanguageViewModel::class.java]
        languageViewModel.languageCode.observe(viewLifecycleOwner, Observer { newLanguage ->
            val trendingNews = api.getTrendings("General", newLanguage, "in", 1)

            trendingNews.enqueue(object : Callback<News?> {
                override fun onResponse(call: Call<News?>, response: Response<News?>) {
                    val responseBody = response.body()
                    progressBar.visibility = View.GONE

                    val dataList = responseBody?.data ?: emptyList()

                    if (dataList.isNotEmpty()) {
                        val titleList = dataList.map { it.title }.toMutableList()
                        val imageUrlList = dataList.map { it.thumbnail }.toMutableList()
                        val urlList = dataList.map { it.url }.toMutableList()
                        val excerptList = dataList.map { it.excerpt }.toMutableList()
                        val publisherName = dataList.map { it.publisher.name }.toMutableList()
                        val publisherIcon = dataList.map { it.publisher.favicon }.toMutableList()

                        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                        recyclerView.layoutManager = LinearLayoutManager(context)
                        val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, publisherName, publisherIcon, uid, this@HomeFragment)
                        recyclerView.adapter = newsAdapter

                        newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener{
                            override fun onCardClick(position: Int) {
                                val intent = Intent(this@HomeFragment.context, NewsWebView::class.java)
                                intent.putExtra("url", urlList[position])
                                intent.putExtra("uid", uid)
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
        })

        val trendingNews = api.getTrendings("General", savedLanguageCode, "in", 1)

        trendingNews.enqueue(object : Callback<News?> {
            override fun onResponse(call: Call<News?>, response: Response<News?>) {
                val responseBody = response.body()
                progressBar.visibility = View.GONE

                val dataList = responseBody?.data ?: emptyList()

                if (dataList.isNotEmpty()) {
                    val titleList = dataList.map { it.title }.toMutableList()
                    val imageUrlList = dataList.map { it.thumbnail }.toMutableList()
                    val urlList = dataList.map { it.url }.toMutableList()
                    val excerptList = dataList.map { it.excerpt }.toMutableList()
                    val publisherName = dataList.map { it.publisher.name }.toMutableList()
                    val publisherIcon = dataList.map { it.publisher.favicon }.toMutableList()

                    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, publisherName, publisherIcon, uid, this@HomeFragment)
                    recyclerView.adapter = newsAdapter

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener{
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@HomeFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            intent.putExtra("uid", uid)
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
        val savedLanguageCode = arguments?.getString("savedLanguageCode").toString()

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val pageNumber = Random.nextInt(1, 20)
        val api = retrofitBuilder.create(NewsAPI::class.java)
        val trendingNews = api.getTrendings("General", savedLanguageCode, "in", pageNumber)

        trendingNews.enqueue(object : Callback<News?> {
            override fun onResponse(call: Call<News?>, response: Response<News?>) {
                val responseBody = response.body()

                val dataList = responseBody?.data ?: emptyList()

                if (dataList.isNotEmpty()) {
                    val titleList = dataList.map { it.title }.toMutableList()
                    val imageUrlList = dataList.map { it.thumbnail }.toMutableList()
                    val urlList = dataList.map { it.url }.toMutableList()
                    val excerptList = dataList.map { it.excerpt }.toMutableList()
                    val publisherName = dataList.map { it.publisher.name }.toMutableList()
                    val publisherIcon = dataList.map { it.publisher.favicon }.toMutableList()

                    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView?.layoutManager = LinearLayoutManager(context)
                    val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, publisherName, publisherIcon, uid, this@HomeFragment)
                    recyclerView?.adapter = newsAdapter

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener{
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@HomeFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            intent.putExtra("uid", uid)
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