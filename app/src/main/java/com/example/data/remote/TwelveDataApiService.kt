package com.example.data.remote

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TwelveDataApiService {

    @GET("time_series")
    suspend fun getTimeSeries(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("outputsize") outputsize: Int = 100,
        @Query("order") order: String = "ASC",
        @Query("apikey") apiKey: String
    ): Response<TimeSeriesResponse>

    companion object {
        private const val BASE_URL = "https://api.twelvedata.com/"

        fun create(): TwelveDataApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(TwelveDataApiService::class.java)
        }
    }
}
