package edu.utap.stocknewsapp.api

class NewsRepository(private val api: MarketAuxApi) {
    suspend fun getNews(entity: String): List<NewsData> {
        return api.getNews(entity).data
    }
    suspend fun searchEntity(entity: String): List<NewsData> {
        return api.searchEntity(entity).data
    }
}