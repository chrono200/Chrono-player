package com.chronodisrup.chronoplay.core.network

import com.chronodisrup.chronoplay.data.remote.MurielApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun create(baseUrl: String): MurielApi {
        require(baseUrl.startsWith("https://") || baseUrl.startsWith("http://")) {
            "Server URL must start with http:// or https://"
        }
        require(baseUrl.endsWith("/")) { "Server URL must end with /" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MurielApi::class.java)
    }
}
