package com.chronodisrup.chronoplay.core.network

import com.chronodisrup.chronoplay.data.remote.MurielApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    fun create(baseUrl: String, token: String = "hellozhou"): MurielApi {
        require(baseUrl.startsWith("https://") || baseUrl.startsWith("http://")) {
            "Server URL must start with http:// or https://"
        }
        require(baseUrl.endsWith("/")) { "Server URL must end with /" }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().header("X-Muriel-Token", token).build()
                chain.proceed(request)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(MurielApi::class.java)
    }
}