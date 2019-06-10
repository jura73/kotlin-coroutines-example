package com.example.android.kotlincoroutines.main

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitApi {
    companion object {
        private const val BASE_URL = "https://httpstat.us"

        fun service(): RetrofitApi {
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .build()
                    .create(RetrofitApi::class.java)
        }
    }

    @GET("/200")
    fun getOk(@Query("sleep") sleep: Int): Call<Unit>

    @GET("/400")
    fun getBadRequest(@Query("sleep") sleep: Int): Call<Unit>
}