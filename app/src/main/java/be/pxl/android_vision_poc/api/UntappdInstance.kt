package be.pxl.android_vision_poc.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UntappdInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.untappd.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: UntappdApi by lazy {
        retrofit.create(UntappdApi::class.java)
    }
}