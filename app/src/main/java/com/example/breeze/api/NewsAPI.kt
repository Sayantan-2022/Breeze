package com.example.breeze.api

import com.example.breeze.models.News
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NewsAPI {
    @Headers(
        "X-RapidAPI-Key: 8693b24f1emsh90fffe384f7064cp1ab6edjsn2303696c045b",
        "X-RapidAPI-Host: news-api14.p.rapidapi.com"
    )

      @GET("v2/trendings")
      fun getTrendings(
          @Query("topic")
          topic : String,
          @Query("language")
          language : String,
          @Query("country")
          country : String
      ) : Call<News>

      @GET("v2/search/articles")
      fun searchNews(
          @Query("query")
          query : String,
          @Query("language")
          language: String
      ) : Call<News>
}