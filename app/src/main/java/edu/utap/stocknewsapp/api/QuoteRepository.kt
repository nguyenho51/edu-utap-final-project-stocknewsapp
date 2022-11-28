package edu.utap.stocknewsapp.api

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.internal.wait

class QuoteRepository(private val api: FinnHubApi) {

    suspend fun getQuote(entity: String): QuoteResponse {
        return api.getQuote(entity.uppercase())
    }

    suspend fun getMultipleQuote(favList: List<NewsData>): List<NewsData> {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                favList.forEach {
                    val quote = withContext(Dispatchers.Default) {
                            api.getQuote(it.symbol!!.uppercase())
                    }
                    //delay(300) no need to delay because withContext return result before continue
                    it.currentPrice = quote.currentPrice
                    it.dollarChange = quote.dollarChange
                    it.percentChange = quote.percentChange
                }
            } catch (e: ConcurrentModificationException) {
                Log.e("Error", "Favorite list is modified!")
            }
        }
        return favList
    }

}