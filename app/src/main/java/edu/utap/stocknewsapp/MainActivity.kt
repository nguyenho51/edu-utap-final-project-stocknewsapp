package edu.utap.stocknewsapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.navigation.NavController
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import edu.utap.stocknewsapp.databinding.ActivityMainBinding
import edu.utap.stocknewsapp.ui.MainViewModel
import edu.utap.stocknewsapp.usermetadata.AuthInit
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    enum class FragmentTitle
    {
        NEWS,
        FAVORITE,
        SETTING
    }
    private fun initBottomNavigationEnabler() {
        viewModel.observeFragTitle().observe(this)
        {
            when (viewModel.observeFragTitle().value)
            {
                FragmentTitle.NEWS ->
                {
                    binding.newsBut.isClickable = false
                    binding.favBut.isClickable = true
                    binding.accountBut.isClickable = true
                }
                FragmentTitle.FAVORITE ->
                {
                    binding.newsBut.isClickable = true
                    binding.favBut.isClickable = false
                    binding.accountBut.isClickable = true
                }
                else ->
                {
                    binding.newsBut.isClickable = true
                    binding.favBut.isClickable = true
                    binding.accountBut.isClickable = false
                }
            }
        }
    }
    private fun initBottomButtonListener(navController: NavController) {
        binding.newsBut.setOnClickListener {
            //Log.d("XXX Current Destination", "${navController.currentDestination?.label}")
            navController.popBackStack()
            navController.navigate(R.id.NewsFragment)
            viewModel.setFragTitle(FragmentTitle.NEWS)
        }
        binding.favBut.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.FavoriteFragment)
            viewModel.setFragTitle(FragmentTitle.FAVORITE)
        }
        binding.accountBut.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.AccountFragment)
            viewModel.setFragTitle(FragmentTitle.SETTING)
        }
    }

    private fun initNewsUpdateObserver() {
        viewModel.observeUserMeta().observe(this) {
            viewModel.loadUserMeta()
        }
    }

    // An Android nightmare
    // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    // https://stackoverflow.com/questions/7789514/how-to-get-activitys-windowtoken-without-view
    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }

    // See: https://developer.android.com/training/basics/intents/result
    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
            viewModel.loadUserInfo()
        }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        initBottomNavigationEnabler()
        initBottomButtonListener(navController)
        initNewsUpdateObserver()
        viewModel.fetchQuote()

        onBackPressedDispatcher.addCallback(this) {
            // Handle the system back button
            navController.popBackStack()
            navController.navigate(R.id.NewsFragment)
            viewModel.setFragTitle(FragmentTitle.NEWS)
        }
        AuthInit(viewModel, signInLauncher)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}