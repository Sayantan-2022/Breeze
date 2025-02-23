package com.example.breeze.ui.search

import android.R.attr
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.breeze.MainActivity
import com.example.breeze.R
import com.example.breeze.adapter.NewsAdapter
import com.example.breeze.api.NewsAPI
import com.example.breeze.models.LanguageViewModel
import com.example.breeze.models.News
import com.example.breeze.ui.NewsWebView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Runnable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale


class SearchFragment : Fragment(R.layout.fragment_search), SwipeRefreshLayout.OnRefreshListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar2)
        progressBar.visibility = View.VISIBLE

        val uid = arguments?.getString("uid").toString()
        val savedLanguageCode = arguments?.getString("savedLanguageCode").toString()
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        val etQuery = view.findViewById<TextInputEditText>(R.id.etQuery)
        val btnVoiceSearch = view.findViewById<ImageButton>(R.id.btnVoiceSearch)
        val tvVoiceSearch = view.findViewById<TextView>(R.id.tvVoiceSearch)

        setupTabLayout(uid)

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)

        val trendingNews = api.getTrendings("General", savedLanguageCode, "in", 2)
        loadNews(trendingNews, view, uid)

        etQuery.addTextChangedListener {
            progressBar.visibility = View.VISIBLE
            val searchedQuery = etQuery.text.toString().trim()
            if (searchedQuery.isEmpty()) loadNews(trendingNews, view, uid)
            else{
                val searchedNews = api.searchNews(searchedQuery, savedLanguageCode)
                loadNews(searchedNews, view, uid)
            }
        }

        btnVoiceSearch.setOnClickListener {
            startVoiceRecognition()
        }

        tvVoiceSearch.setOnClickListener {
            startVoiceRecognition()
        }

        swipeRefreshLayout.setOnRefreshListener(this@SearchFragment)
    }

    private fun loadNews(newsCall: Call<News>, view: View, uid : String) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar2)

        newsCall.enqueue(object : Callback<News?> {
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

                    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    val newsAdapter = NewsAdapter(titleList, imageUrlList, excerptList, urlList, publisherName, publisherIcon, uid, this@SearchFragment)
                    recyclerView.adapter = newsAdapter
                    progressBar.visibility = View.GONE

                    newsAdapter.setOnCardClickListener(object : NewsAdapter.onCardClickListener {
                        override fun onCardClick(position: Int) {
                            val intent = Intent(this@SearchFragment.context, NewsWebView::class.java)
                            intent.putExtra("url", urlList[position])
                            intent.putExtra("uid", uid)
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
        val savedLanguageCode = arguments?.getString("savedLanguageCode").toString()

        val etQuery = view?.findViewById<TextInputEditText>(R.id.etQuery)
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)

        val searchedQuery = etQuery?.text.toString().trim()

        if (searchedQuery.isNotEmpty()) {
            val searchedNews = api.searchNews(searchedQuery, savedLanguageCode)
            view?.let { loadNews(searchedNews, it, uid) }
        } else {
            view?.let { Snackbar.make(it, "Empty Search!", Toast.LENGTH_SHORT).show() }
        }

        val swipeRefreshLayout = view?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        Handler().postDelayed(Runnable { swipeRefreshLayout?.isRefreshing = false }, 2000)
    }

    private fun setupTabLayout(uid: String) {
        val tabLayout = view?.findViewById<TabLayout>(R.id.tab_layout)
        val savedLanguageCode = arguments?.getString("savedLanguageCode").toString()
        val categories = listOf("Business", "Sports", "Health", "Politics")

        for (category in categories) {
            tabLayout?.addTab(tabLayout.newTab().setText(category))
        }

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(NewsAPI::class.java)

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.text?.let {
                    val trendingNews = api.getTrendings(it.toString(), savedLanguageCode, "in", 1)

                    view?.let { it1 -> loadNews(trendingNews, it1, uid) }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun startVoiceRecognition(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        try {
            voiceRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this@SearchFragment.context,
                "Voice search is not supported on this device.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val voiceRecognitionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val etQuery = view?.findViewById<TextInputEditText>(R.id.etQuery)
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            etQuery?.setText(spokenText)
        }
    }
}