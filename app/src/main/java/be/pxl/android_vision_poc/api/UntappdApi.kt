package be.pxl.android_vision_poc.api

import be.pxl.android_vision_poc.models.UntappdBeer
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UntappdApi {
    @GET("/v4/search/beer")
    suspend fun search(@Query("q") search: String, @Query("client_id") clientId: String, @Query("client_secret") clientSecret: String): Response<UntappdBeer>
}