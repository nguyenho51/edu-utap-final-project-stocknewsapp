package edu.utap.stocknewsapp.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import edu.utap.stocknewsapp.MainActivity
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
            symbol = "AAPL", name = "Apple Inc.", exchange = "NASDAQ", entities = null)
        private val amzn = NewsData(null, null, null, null, null,
            symbol = "AMZN", name = "Amazon.com, Inc.", exchange = "NASDAQ", entities = null)

        /*
        Create website intent for clicking a news row
         */
        fun goToNews(context: Context, news: NewsData) {
            val url = news.url
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(context,intent,null)
        }
    }

    /*
    Enable click-ability of bottom navigation buttons
     */
    private var fragTitle: MutableLiveData<MainActivity.FragmentTitle> = MutableLiveData(MainActivity.FragmentTitle.NEWS)
    fun setFragTitle(title: MainActivity.FragmentTitle) {
        fragTitle.value = title
    }
    fun observeFragTitle(): MutableLiveData<MainActivity.FragmentTitle> {
        return fragTitle
    }


    /*
    Call this function at login success
     */
    fun signInSuccessful() {
        //isSignIn(true)
        loadUserInfo()
        //loadUserMeta()
    }

    // User's display information in Account Setting fragment
    private var displayName = MutableLiveData("Please log in")
    fun getDisplayName(): MutableLiveData<String> {
        return displayName
    }
    private var email = MutableLiveData("Please log in")
    fun getDisplayEmail(): MutableLiveData<String> {
        return email
    }

    // Not to be displayed, for internal use only
    private var uid = MutableLiveData("Please log in")
    private var favStockList = mutableListOf<NewsData>()
    private var userMeta: MutableLiveData<UserMeta> = MutableLiveData()
    fun observeUserMeta(): MutableLiveData<UserMeta> {
        return userMeta
    }

    // Database access
    private val dbHelp = ViewModelDBHelper()

    /* TODO find use for this later
    private var signIn = MutableLiveData(false)
    fun observeSignInSuccess(): MutableLiveData<Boolean> {
        return signIn
    }
    private fun isSignIn(isSignIn: Boolean) {
        signIn.value = isSignIn
    }

     */

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
        favStockList = userMeta.value?.ownerFavStockList ?: mutableListOf(aapl, amzn)
        favUpdated()
        Log.d(javaClass.simpleName, "In side viewModel, " +
                    "user meta posted successfully? ${userMeta.value}")
        fetchNews()
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
    }


    /******************* THIS PART HANDLES ANYTHING RELATED TO NEWS ********************/
    /***********************************************************************************/
    // Initiate network variables
    private val api = MarketAuxApi.create()
    private val repository = Repository(api)
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

    private fun fetchNews() {
        viewModelScope.launch(context = viewModelScope.coroutineContext
                    + Dispatchers.IO)
        {
            favStockList.chunked(2).forEach {
                // look complicated than it should be, but it does the job for
                // when a list contains data class instead of primitive type
                val symbol = it.asSequence().map(NewsData::symbol).joinToString(",")
                netNews.postValue(repository.getNews(symbol))
            }
        }
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
        favNews.clear()
        fetchNews()
        newsUpdated()
        createOrUpdateUserMeta()
    }
    fun removeFavorite(entity: Int) {
        favStockList.removeAt(entity)
        favUpdated()
        favNews.clear()
        fetchNews()
        newsUpdated()
        createOrUpdateUserMeta()
    }
    fun observeFavStocksList(): MutableLiveData<List<NewsData>> {
        return favStocksLiveList
    }



}