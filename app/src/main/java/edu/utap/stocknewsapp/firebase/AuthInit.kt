package edu.utap.stocknewsapp.firebase

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import edu.utap.stocknewsapp.ui.MainViewModel

class AuthInit(viewModel: MainViewModel, signInLauncher: ActivityResultLauncher<Intent>) {
    companion object {
        private const val TAG = "AuthInit"
        fun setDisplayName(displayName : String, viewModel: MainViewModel) {
            Log.d(TAG, "XXX profile change request")
            val nameUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            FirebaseAuth.getInstance().currentUser?.updateProfile(nameUpdate)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User name updated.")
                        viewModel.loadUserInfo()
                        viewModel.createOrUpdateUserMeta()
                    }
                }
        }

        fun changeUserPassword(newPassword: String /*, viewModel: MainViewModel*/ ) {
            FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User password updated.")
                    }
                }
        }
    }

    init {
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null) {
            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build())
            // Create and launch sign-in intent
            // Set authentication providers and start sign-in for user
            // setIsSmartLockEnabled(false) solves some problems
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build()
            signInLauncher.launch(signInIntent)
        } else {
            // User already signed in
            viewModel.setIsLoggedIn(true)
            viewModel.loadUserInfo()
        }
    }

}