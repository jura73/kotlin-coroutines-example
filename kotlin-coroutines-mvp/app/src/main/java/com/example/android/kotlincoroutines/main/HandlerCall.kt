package com.example.android.kotlincoroutines.main

import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response

class HandlerCall<T>(private val call: Call<T>) : RunnableFuture<T> {
    override fun run(): T? {

        val response = if (call.isExecuted) call.clone().execute() else call.execute()
        if (response.isSuccessful) {
            return response.body()
        }
        throw HttpException(response)
    }

    override fun cancel() {
        call.cancel()
    }
}

class HandlerCallResponse<T>(private val call: Call<T>) : RunnableFuture<Response<T?>> {
    override fun run(): Response<T?> {
        return if (call.isExecuted) call.clone().execute() else call.execute()
    }

    override fun cancel() {
        call.cancel()
    }
}

interface RunnableFuture<T> {
    fun run(): T?
    fun cancel()
}