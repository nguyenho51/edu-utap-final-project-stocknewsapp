package edu.utap.stocknewsapp.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import edu.utap.stocknewsapp.MainActivity
import edu.utap.stocknewsapp.api.*
import edu.utap.stocknewsapp.usermetadata.UserMeta
import edu.utap.stocknewsapp.usermetadata.ViewModelDBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    companion object {
        // Example entities
        private val aapl = NewsData(null, null, null, null, null,
                            symbol = "AAPL", name = "Apple Inc.", exchange = "NASDAQ", entities = null,
                            currentPrice = null, dollarChange = null, percentChange = null)
        private val amzn = NewsData(null, null, null, null, null,
                            symbol = "AMZN", name = "Amazon.com, Inc.", exchange = "NASDAQ", entities = null,
                            currentPrice = null, dollarChange = null, percentChange = null)
        fun goToNews(context: Context, news: NewsData) {
            // call this function to create webpage intent
            val url = news.url
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(context,intent,null)
        }
    }


    /*
    -----Enable/Disable click-ability of bottom navigation buttons-----
     */
    private var fragTitle: MutableLiveData<MainActivity.FragmentTitle> = MutableLiveData(MainActivity.FragmentTitle.NEWS)
    fun setFragTitle(title: MainActivity.FragmentTitle) {
        fragTitle.value = title
    }
    fun observeFragTitle(): MutableLiveData<MainActivity.FragmentTitle> {
        return fragTitle
    }


    /*
    -----Account Fragment's content builder-----
     */
    private var displayName = MutableLiveData("Please log in")
    fun getDisplayName(): MutableLiveData<String> {
        return displayName
    }
    private var email = MutableLiveData("Please log in")
    fun getDisplayEmail(): MutableLiveData<String> {
        return email
    }
    private var uid = MutableLiveData("Please log in") // Not to be displayed
    private var favStockList = mutableListOf<NewsData>()
    private var userMeta: MutableLiveData<UserMeta> = MutableLiveData()
    fun observeUserMeta(): MutableLiveData<UserMeta> {
        return userMeta
    }

    private val dbHelp = ViewModelDBHelper() // Database access

    fun loadUserInfo() {
        // Call this function after the user logins and after they updated display name
        // Can also implement a delay after pushing new data to cloud before call to get updates
        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                   + Dispatchers.Default){
            delay(500)
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            fetchUserMeta()
            displayName.postValue(currentUser.displayName)
            email.postValue(currentUser.email)
            uid.postValue(currentUser.uid)
        }
    }
    fun loadUserMeta() {
        // Call this function when the user successfully logged-in
        favStockList = userMeta.value?.ownerFavStockList ?: mutableListOf(aapl, amzn)
        fetchNews()
        favUpdated()
    }
    fun createOrUpdateUserMeta() {
        // Call this function when the logged-on user updates their favorite stock list
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val userMeta = UserMeta(
            ownerUid = currentUser.uid,
            ownerName = currentUser.displayName?: "Anonymous user",
            ownerEmail = currentUser.email!!,
            ownerFavStockList = favStockList,
        )
        dbHelp.createOrUpdateUserMeta(userMeta)
    }
    private fun fetchUserMeta() {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        dbHelp.fetchUserMeta(currentUser.uid,userMeta)
    }
    fun signOut() {
        // call this function when user signs out
        createOrUpdateUserMeta()
        FirebaseAuth.getInstance().signOut()
        favNewsLiveData.value = null
    }


    /*
    -----News Fragment's content builder-----
     */
    private val newsApi = MarketAuxApi.create()
    private val newsRepository = NewsRepository(newsApi)
    private var netNews: MutableLiveData<List<NewsData>> = MutableLiveData()
    private var favNews = mutableListOf<NewsData>()
    private var newsUpdated: MutableLiveData<Boolean?> = MutableLiveData()
    private var favNewsLiveData = MediatorLiveData<List<NewsData>>().apply {
        addSource(netNews) {  netNews.value?.let {
                                favNews.addAll(it)
                                newsUpdated()}  }
        addSource(newsUpdated) {value = favNews.distinctBy { it.uuid }}
    }

    fun observeLiveNews(): MediatorLiveData<List<NewsData>> {
        return favNewsLiveData
    }
    fun newsUpdated() {
        val updated = newsUpdated.value
        newsUpdated.value = updated
    }
    fun fetchNews() {
        viewModelScope.launch(context = viewModelScope.coroutineContext
                    + Dispatchers.IO)
        {
            favStockList.chunked(3).forEach {
                // look complicated than it should be, but it does the job for
                // when a list contains data class instead of primitive type
                val symbol = it.asSequence().map(NewsData::symbol).joinToString(",")
                netNews.postValue(newsRepository.getNews(symbol))
            }
        }
    }


    /*
    -----Search and find stock symbol-----
     */
    private var foundEntity: MutableLiveData<List<NewsData>?> = MutableLiveData()
    fun observeFoundEntity(): MutableLiveData<List<NewsData>?> {
        return foundEntity
    }
    private var searchTerm = MutableLiveData<String?>()
    fun fetchSearch(term: String) {
        searchTerm.value = term
        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                    + Dispatchers.IO) {
            foundEntity.postValue(newsRepository.searchEntity(searchTerm.value!!))
        }
    }
    fun reinitializeSearchVars() {
        // Fragment Navigator tends to recreate fragment everytime we go from another fragment
        // It's annoying if the below vars aren't reset to null on Favorite Frag's destroy
        foundEntity.value = null
        searchTerm.value = null
    }


    /*
    -----Favorite fragment's contents handler-----
     */
    private val quoteApi = FinnHubApi.create()
    private val quoteRepository = QuoteRepository(quoteApi)
    private var favUpdated: MutableLiveData<Boolean?> = MutableLiveData()
    private fun favUpdated() {
        val found = favUpdated.value
        favUpdated.value = found
    }

    private var listFavStockWithQuote = MediatorLiveData<List<NewsData>>().apply {
        addSource(favUpdated) {value = favStockList}
    }
    fun isFavoriteByName(entityName: String): Boolean {
        for (entity in favStockList) {
            if (entity.symbol?.lowercase() == entityName.lowercase()) { return true }
        }
        return false
    }
    fun addFavorite(entity: NewsData, context: Context?) {
        if (favStockList.size < 10 ) {
            favStockList.add(entity)
            favUpdated()
            favNews.clear()
            fetchNews()
            createOrUpdateUserMeta()
        } else {
            Toast.makeText(
                context, "Watchlist is full!",Toast.LENGTH_SHORT).show()
        }
    }
    fun removeFavorite(entity: Int) {
        favStockList.removeAt(entity)
        favUpdated()
        favNews.clear()
        fetchNews()
        createOrUpdateUserMeta()
    }
    fun observeFavStocksList(): MutableLiveData<List<NewsData>> {
        return listFavStockWithQuote
    }

    fun fetchQuote() {
        // Call this function when the apps starts
        viewModelScope.launch(context = viewModelScope.coroutineContext
                + Dispatchers.IO)
        {
            while (isActive) {
                try {
                    listFavStockWithQuote.postValue(quoteRepository.getMultipleQuote(favStockList))
                    delay(15000)
                } catch (e: ConcurrentModificationException) {
                    Log.e("Error", "Favorite list is modified!")
                } catch (e: java.lang.NullPointerException) {
                    Log.e("Error", "Favorite list is empty!")
                }
            }
        }
    }

} // end of Main VM