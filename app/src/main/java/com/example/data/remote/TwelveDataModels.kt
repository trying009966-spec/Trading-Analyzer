package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CandleValue(
    @Json(name = "datetime") val datetime: String,
    @Json(name = "open") val open: String,
    @Json(name = "high") val high: String,
    @Json(name = "low") val low: String,
    @Json(name = "close") val close: String,
    @Json(name = "volume") val volume: String? = null
)

@JsonClass(generateAdapter = true)
data class TimeSeriesResponse(
    @Json(name = "values") val values: List<CandleValue>? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "code") val code: Int? = null
)
