package com.example.breeze.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.breeze.R
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.api.NewsAPI
import com.example.breeze.models.News
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view : View,savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "onViewCreated called")

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)
        val trendingNews = api.getTrendings("General", "en", "in")

        trendingNews.enqueue(object : Callback<News?> {
            override fun onResponse(call: Call<News?>, response: Response<News?>) {
                Log.d("HomeFragment", "API call successful")
                val responseBody = response.body()
                Log.d("HomeFragment", "Response body: $responseBody")

                val dataList = responseBody?.data ?: emptyList()

                if (dataList.isNotEmpty()) {
                    val titleList = dataList.map { it.title }.toMutableList()
                    val imageUrlList = dataList.map { it.thumbnail }.toMutableList()
                    val urlList = dataList.map { it.url }.toMutableList()

                    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = NewsAdapter(titleList, imageUrlList, urlList, this@HomeFragment)

                    Log.d("HomeFragment", "RecyclerView adapter attached")
                } else {
                    Log.d("HomeFragment", "No data found in API response")
                }
            }

            override fun onFailure(call: Call<News?>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
            }
        })
    }
}