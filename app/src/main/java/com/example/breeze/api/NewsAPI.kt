package com.example.breeze.api

import com.example.breeze.models.News
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NewsAPI {
    @Headers(
        "X-RapidAPI-Key: f4b81ba75cmsh2844ffd658abb7cp1b7d18jsn82f5982231a6",
        "X-RapidAPI-Host: news-api14.p.rapidapi.com"
    )

      @GET("v2/trendings")
      fun getTrendings(
          @Query("topic")
          topic : String,
          @Query("language")
          language : String = "en",
          @Query("country")
          country : String,
          @Query("page")
          page : Int
      ) : Call<News>

    @Headers(
          "X-RapidAPI-Key: f4b81ba75cmsh2844ffd658abb7cp1b7d18jsn82f5982231a6",
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