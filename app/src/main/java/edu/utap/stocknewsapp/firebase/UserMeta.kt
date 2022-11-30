package edu.utap.stocknewsapp.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import edu.utap.stocknewsapp.api.NewsData

data class UserMeta (

    // Firebase insists we have a no argument constructor
    // Auth information
    var ownerUid: String = "",
    var ownerName: String = "",
    var ownerEmail: String = "",
    var ownerFavStockList: MutableList<NewsData> = mutableListOf(),

    // Written on the server
    @ServerTimestamp val timeStamp: Timestamp? = null,
)