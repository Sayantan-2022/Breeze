package com.example.breeze.api

import com.example.breeze.models.News
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NewsAPI {
    @Headers(
        "X-RapidAPI-Key: a123d43e74mshead248ff5ed7b70p1eea2djsnf086cf973dab",
        "X-RapidAPI-Host: news-api14.p.rapidapi.com"
    )

      @GET("v2/trendings")
      fun getTrendings(
          @Query("topic")
          topic : String,
          @Query("language")
          language : String,
          @Query("country")
          country : String,
          @Query("page")
          page : Int
      ) : Call<News>

    @Headers(
          "X-RapidAPI-Key: a123d43e74mshead248ff5ed7b70p1eea2djsnf086cf973dab",
          "X-RapidAPI-Host: news-api14.p.rapidapi.com"
    )
      @GET("v2/search/articles")
      fun searchNews(
          @Query("query")
          query : String = "sports",
          @Query("language")
          language: String = "en"
      ) : Call<News>
}