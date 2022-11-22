package edu.utap.stocknewsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.viewModels
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import edu.utap.stocknewsapp.databinding.ActivityMainBinding
import edu.utap.stocknewsapp.ui.MainViewModel
import edu.utap.stocknewsapp.usermetadata.AuthInit

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val newsTitle = "News"
    private val favTitle = "Favorite"
    private val accountTitle = "Account Setting"
    private val viewModel: MainViewModel by viewModels()

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
            viewModel.signInSuccessful()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Control buttons
        onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            navController.navigate(R.id.NewsFragment)
        }
        binding.newsBut.setOnClickListener {
            navController.navigate(R.id.NewsFragment)
        }
        binding.favBut.setOnClickListener {
            navController.navigate(R.id.FavoriteFragment)
        }
        binding.accountBut.setOnClickListener {
            navController.navigate(R.id.AccountFragment)
        }

        AuthInit(viewModel, signInLauncher)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Back button redirect to News Fragment
        navController.navigate(R.id.NewsFragment)
        return true
        //navController.navigateUp(appBarConfiguration)
        //        || super.onSupportNavigateUp()
    }

}