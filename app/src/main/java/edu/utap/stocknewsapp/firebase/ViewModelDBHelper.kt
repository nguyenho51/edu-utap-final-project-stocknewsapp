package edu.utap.stocknewsapp.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class ViewModelDBHelper() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val rootCollection = "allUserMeta"

    fun fetchUserMeta(userUID: String, userMeta: MutableLiveData<UserMeta>) {
        dbFetchUserMeta(userUID, userMeta)
    }

    // If we want to listen for real time updates use this
    // .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
    // But be careful about how listener updates live data
    // and noteListener?.remove() in onCleared
    private fun dbFetchUserMeta(userUID: String, userMeta: MutableLiveData<UserMeta>) {
        val userRef = db.collection(rootCollection).document(userUID)
        userRef.get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "User meta fetch ${result.data}")
                val retrievedUserData = result.toObject(UserMeta::class.java)
                userMeta.value = retrievedUserData
                //Log.d(javaClass.simpleName, "User meta posted successfully? ${userMeta.value}")
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "user meta fetch FAILED ", it)
            }
    }

    // https://firebase.google.com/docs/firestore/manage-data/add-data#add_a_document
    fun createOrUpdateUserMeta(userMeta: UserMeta) {
        // Add/update userMeta
        db.collection(rootCollection)
            .document(userMeta.ownerUid)
            .set(userMeta)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "User meta successfully created!")
            }
            .addOnFailureListener {
                    e -> Log.w(javaClass.simpleName, "Error creating/updating user meta", e)
            }
    }
}