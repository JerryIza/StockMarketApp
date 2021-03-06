package com.example.apistock.data.entities


import com.squareup.moshi.Json

data class SymbolDetails(
    val askId: String,
    val askPrice: Double,
    val askSize: Int,
    val assetMainType: String,
    val assetType: String,
    val bidId: String,
    val bidPrice: Double,
    val bidSize: Int,
    val bidTick: String,
    val closePrice: Double,
    val cusip: String,
    val delayed: Boolean,
    val description: String,
    val digits: Int,
    val divAmount: Double,
    val divDate: String,
    val divYield: Double,
    val exchange: String,
    val exchangeName: String,
    val highPrice: Double,
    val lastId: String,
    val lastPrice: Double,
    val lastSize: Int,
    val lowPrice: Double,
    val marginable: Boolean,
    val mark: Double,
    val markChangeInDouble: Double,
    val markPercentChangeInDouble: Double,
    val nAV: Double,
    val netChange: Double,
    val netPercentChangeInDouble: Double,
    val openPrice: Double,
    val peRatio: Double,
    val quoteTimeInLong: Long,
    val regularMarketLastPrice: Double,
    val regularMarketLastSize: Int,
    val regularMarketNetChange: Double,
    val regularMarketPercentChangeInDouble: Double,
    val regularMarketTradeTimeInLong: Long,
    val securityStatus: String,
    val shortable: Boolean,
    val symbol: String,
    val totalVolume: Int,
    val tradeTimeInLong: Long,
    //ok? front, back, implied, historical??? basically useless since we don't know what its based off of.
    val volatility: Double,
    @field:Json(name = "52WkHigh")
    val wkHigh: Double,
    @field:Json(name = "52WkLow")
    val wkLow: Double
)