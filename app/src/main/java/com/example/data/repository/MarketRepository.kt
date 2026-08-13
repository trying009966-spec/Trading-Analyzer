package com.example.data.repository

import com.example.BuildConfig
import com.example.data.remote.TwelveDataApiService
import com.example.domain.trading.Candle

sealed class MarketDataResult {
    data class Success(val candles: List<Candle>) : MarketDataResult()
    data class Error(val message: String) : MarketDataResult()
}

class MarketRepository(private val apiService: TwelveDataApiService = TwelveDataApiService.create()) {

    val supportedSymbols = listOf(
        "BTC/USD",
        "EUR/USD",
        "XAU/USD",
        "AAPL",
        "TSLA",
        "NVDA"
    )

    val supportedTimeframes = listOf("1m", "5m", "15m", "1H", "4H", "1D")

    private val timeframeIntervalMap = mapOf(
        "1m" to "1min",
        "5m" to "5min",
        "15m" to "15min",
        "1H" to "1h",
        "4H" to "4h",
        "1D" to "1day"
    )

    suspend fun fetchMarketCandles(symbol: String, timeframe: String): MarketDataResult {
        val apiKey = BuildConfig.TWELVE_DATA_API_KEY
        if (apiKey.isBlank() || apiKey == "YOUR_TWELVE_DATA_API_KEY_HERE") {
            return MarketDataResult.Error("Twelve Data API Key is missing. Please configure TWELVE_DATA_API_KEY in secrets/environment.")
        }

        val interval = timeframeIntervalMap[timeframe] ?: "1h"

        return try {
            val response = apiService.getTimeSeries(
                symbol = symbol,
                interval = interval,
                outputsize = 100,
                order = "ASC",
                apiKey = apiKey
            )

            if (!response.isSuccessful) {
                return MarketDataResult.Error("API HTTP ${response.code()}: ${response.message()}")
            }

            val body = response.body()
            if (body == null || body.status == "error" || body.values.isNullOrEmpty()) {
                val msg = body?.message ?: "Failed to load market data for $symbol."
                return MarketDataResult.Error(msg)
            }

            val candles = body.values.mapNotNull { value ->
                val open = value.open.toDoubleOrNull()
                val high = value.high.toDoubleOrNull()
                val low = value.low.toDoubleOrNull()
                val close = value.close.toDoubleOrNull()

                if (open != null && high != null && low != null && close != null) {
                    Candle(
                        timestamp = value.datetime,
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = value.volume?.toDoubleOrNull()
                    )
                } else null
            }

            if (candles.isEmpty()) {
                MarketDataResult.Error("No valid candle data returned for $symbol.")
            } else {
                MarketDataResult.Success(candles)
            }
        } catch (e: Exception) {
            MarketDataResult.Error("Network error: ${e.localizedMessage ?: "Unable to connect to market server."}")
        }
    }

    suspend fun testApiConnection(): Boolean {
        val apiKey = BuildConfig.TWELVE_DATA_API_KEY
        if (apiKey.isBlank()) return false
        return try {
            val response = apiService.getTimeSeries(
                symbol = "BTC/USD",
                interval = "1h",
                outputsize = 1,
                order = "ASC",
                apiKey = apiKey
            )
            response.isSuccessful && response.body()?.values?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }
}
