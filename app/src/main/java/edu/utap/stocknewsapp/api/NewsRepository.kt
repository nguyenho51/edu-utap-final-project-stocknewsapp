package edu.utap.stocknewsapp.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsRepository(private val api: MarketAuxApi) {

    suspend fun getNewsToStart(favList: List<NewsData>): List<NewsData> {
        // call this function to fetch news when the app starts
        val symbol = favList.asSequence().map(NewsData::symbol).joinToString(",")
        return api.getNewsToStart(symbol).data
    }

    suspend fun getNews(favList: List<NewsData>, favNewsList: MutableList<NewsData>): List<NewsData> {
        // call this function to fetch news from the network
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Might be useful to fetch 2 symbols at once to reduce number of network requests
                favList.chunked(2).forEach {
                    val symbol = it.asSequence().map(NewsData::symbol).joinToString(",")
                    val fetchedNews = withContext(Dispatchers.Default) {
                        api.getNews(symbol)
                    }
                    favNewsList.addAll(fetchedNews.data)
                }
                /*
                favList.forEach {
                    val fetchedNews = withContext(Dispatchers.Default) {
                        api.getNews(it.symbol!!)
                    }
                    favNewsList.addAll(fetchedNews.data)
                }
                 */
            } catch (e: ConcurrentModificationException) {
                Log.e("Error", "Favorite list is modified!")
            }
        }
        return favNewsList
    }

    suspend fun searchEntity(entity: String): List<NewsData> {
        // call this function inside search functionality
        return api.searchEntity(entity).data
    }
}