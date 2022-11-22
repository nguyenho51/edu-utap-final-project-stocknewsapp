package edu.utap.stocknewsapp.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import edu.utap.stocknewsapp.api.MarketAuxApi
import edu.utap.stocknewsapp.api.NewsData
import edu.utap.stocknewsapp.api.Repository
import edu.utap.stocknewsapp.usermetadata.UserMeta
import edu.utap.stocknewsapp.usermetadata.ViewModelDBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    companion object {
        // Example entities
        private val aapl = NewsData(null, null, null, null, null,
            symbol = "AAPL", name = "Apple Inc.", exchange = "NASDAQ")
        private val amzn = NewsData(null, null, null, null, null,
            symbol = "AMZN", name = "Amazon.com, Inc.", exchange = "NASDAQ")

        // HANDLE CLICKING A NEWS TO OPEN UP INTENT TO THE NEWS ARTICLE //
        fun goToNews(context: Context, news: NewsData) {
            val url = news.url
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(context,intent,null)
        }
    }

    /******************** THIS PART HANDLES GENERAL FUNCTIONALITIES ********************/
    fun signInSuccessful() {
        isSignIn(true)
        loadUserInfo()
        loadUserMeta()
    }

    /*********** THIS PART HANDLES ANYTHING RELATED TO AUTHENTICATING A USER ***********/
    // User information
    private var displayName = MutableLiveData("Please log in")
    fun getDisplayName(): MutableLiveData<String> {
        return displayName
    }
    private var email = MutableLiveData("Please log in")
    fun getDisplayEmail(): MutableLiveData<String> {
        return email
    }
    private var uid = MutableLiveData("Please log in")
    private var favStockList = mutableListOf<NewsData>()
    private var userMeta: MutableLiveData<UserMeta> = MutableLiveData()

    // Database access
    private val dbHelp = ViewModelDBHelper()

    private var signIn = MutableLiveData(false)
    fun observeSignInSuccess(): MutableLiveData<Boolean> {
        return signIn
    }
    private fun isSignIn(isSignIn: Boolean) {
        signIn.value = isSignIn
    }

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

    private fun loadUserMeta() {
        // Kick start some forever observers
        this.userMeta.observeForever {
            favStockList = userMeta.value?.ownerFavStockList ?: mutableListOf(aapl,amzn)
            favUpdated()
            Log.d(javaClass.simpleName, "In side viewModel, " +
                    "user meta posted successfully? ${userMeta.value}")
            fetchNews()
        }
        this.observeNews().observeForever {
            netNews.value?.let { it1 -> favNews.addAll(it1)
                newsUpdated()
            }
        }
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
        // Call this function when the user logins
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        dbHelp.fetchUserMeta(currentUser.uid,userMeta)
    }

    fun signOut() {
        createOrUpdateUserMeta()
        FirebaseAuth.getInstance().signOut()
        favNewsLiveData.value = null
        isSignIn(false)
    }
    /***********************************************************************************/


    /******************* THIS PART HANDLES ANYTHING RELATED TO NEWS ********************/
    /***********************************************************************************/
    // Initiate network variables
    private val api = MarketAuxApi.create()
    private val repository = Repository(api)
    private var netNews: MutableLiveData<List<NewsData>> = MutableLiveData()
    private var favNews = mutableListOf<NewsData>()
    private var newsUpdated: MutableLiveData<Boolean?> = MutableLiveData()
    private var favNewsLiveData = MediatorLiveData<List<NewsData>>().apply {
        addSource(newsUpdated) {value = favNews.distinctBy { it.uuid }}
    }

    fun observeLiveNews(): MediatorLiveData<List<NewsData>> {
        return favNewsLiveData
    }
    fun newsUpdated() {
        val updated = newsUpdated.value
        newsUpdated.value = updated
    }

    private fun fetchNews() {
        for (entity in favStockList) {
            viewModelScope.launch(context = viewModelScope.coroutineContext
                        + Dispatchers.IO) {
                netNews.postValue(repository.getNews(entity.symbol!!))
            }
        }
    }
    private fun observeNews(): LiveData<List<NewsData>> {
        return netNews
    }


    /****************** THIS PART HANDLES ANYTHING RELATED TO FAVORITE LIST ************/
    /***********************************************************************************/
    private var foundEntity: MutableLiveData<List<NewsData>?> = MutableLiveData()
    fun observeFoundEntity(): MutableLiveData<List<NewsData>?> {
        return foundEntity
    }
    private var favUpdated: MutableLiveData<Boolean?> = MutableLiveData()
    private fun favUpdated() {
        val found = favUpdated.value
        favUpdated.value = found
    }
    private var favStocksLiveList = MediatorLiveData<List<NewsData>>().apply {
        addSource(favUpdated) {value = favStockList}
    }
    private var searchTerm = MutableLiveData<String?>()
//    fun observeSearchTerm(): MutableLiveData<String?> {
//        return searchTerm
//    }
    fun reinitializeSearchVars() {
        // Fragment Navigator tends to recreate fragment everytime we go from another fragment
        // It's annoying if the below vars aren't reset to null on Favorite Frag's destroy
        foundEntity.value = null
        searchTerm.value = null
    }

    fun fetchSearch(term: String) {
        searchTerm.value = term
        // This is where the network request for search entity is initiated.
        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                    + Dispatchers.IO) {
            foundEntity.postValue(repository.searchEntity(searchTerm.value!!))
        }
    }
    fun isFavoriteByName(entityName: String): Boolean {
        for (entity in favStockList) {
            if (entity.symbol?.lowercase() == entityName.lowercase()) { return true }
        }
        return false
    }
    fun addFavorite(entity: NewsData) {
        favStockList.add(entity)
        favUpdated()
        favNewsLiveData.value = null
        fetchNews()
        createOrUpdateUserMeta()
        //Log.d("XXX", "Favorite List: $favStockList")
    }
    fun removeFavorite(entity: Int) {
        favStockList.removeAt(entity)
        favUpdated()
        favNewsLiveData.value = null
        fetchNews()
        createOrUpdateUserMeta()
        //Log.d("XXX", "Favorite List: $favStockList")
    }
    fun observeFavStocksList(): MutableLiveData<List<NewsData>> {
        return favStocksLiveList
    }



}