package edu.utap.stocknewsapp.api

import com.google.gson.annotations.SerializedName

class QuoteResponse(
    // Useful for getting news
    @SerializedName("c")
    val currentPrice: String? = null,
    @SerializedName("d")
    val dollarChange: String? = null,
    @SerializedName("dp")
    val percentChange: String? = null,
    @SerializedName("h")
    val highPrice: String? = null,
    @SerializedName("l")
    val lowPrice: String? = null,
    @SerializedName("o")
    val openPrice: String? = null,

)