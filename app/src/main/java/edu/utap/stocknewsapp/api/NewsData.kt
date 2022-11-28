package edu.utap.stocknewsapp.api

import android.text.SpannableString
import com.google.gson.annotations.SerializedName

class NewsData (
    // Useful for MarketAux getting news
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("title")
    val title: SpannableString? = null,
    @SerializedName("description")
    val description: SpannableString? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("image_url")
    val image_url: String? = null,
    @SerializedName("entities")
    val entities: List<NewsData>? = null,

    // Useful for MarketAux search query
    @SerializedName("symbol")
    val symbol: String? = "",
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("exchange")
    val exchange: String? = "",

    // Useful for FinnHub api
    var currentPrice: String? = null,
    var dollarChange: String? = null,
    var percentChange: String? = null
) {
    companion object {
        // NB: This only highlights the first match in a string
        fun spannableStringsEqual(a: SpannableString?, b: SpannableString?): Boolean {
            if (a == null && b == null) return true
            if (a == null && b != null) return false
            if (a != null && b == null) return false
            val spA = a!!.getSpans(0, a.length, Any::class.java)
            val spB = b!!.getSpans(0, b.length, Any::class.java)
            return a.toString() == b.toString()
                    &&
                    spA.size == spB.size && spA.equals(spB)
        }
    }
}
        /*
        private fun findAndSetSpan(fulltext: SpannableString, subtext: String): Boolean {
            if (subtext.isEmpty()) return true
            val i = fulltext.indexOf(subtext, ignoreCase = true)
            if (i == -1) return false
            fulltext.setSpan(
                ForegroundColorSpan(Color.CYAN), i, i + subtext.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return true
        }
        */


