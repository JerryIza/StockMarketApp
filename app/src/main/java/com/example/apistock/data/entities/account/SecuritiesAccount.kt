package com.example.apistock.data.entities.account


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SecuritiesAccount(
    @Json(name = "accountId")
    val accountId: String,
    @Json(name = "currentBalances")
    val currentBalances: CurrentBalances,
    @Json(name = "initialBalances")
    val initialBalances: InitialBalances,
    @Json(name = "isClosingOnlyRestricted")
    val isClosingOnlyRestricted: Boolean,
    @Json(name = "isDayTrader")
    val isDayTrader: Boolean,
    @Json(name = "positions")
    val positions: List<Positions>,
    @Json(name = "projectedBalances")
    val projectedBalances: ProjectedBalances,
    @Json(name = "roundTrips")
    val roundTrips: Int,
    @Json(name = "type")
    val type: String
)