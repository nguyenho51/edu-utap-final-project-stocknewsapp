package edu.utap.stocknewsapp.usermetadata

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

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

    /*
    // https://firebase.google.com/docs/firestore/manage-data/delete-data#delete_documents
    // Future update: Letting user delete their account/user meta
    fun removePhotoMeta(
        sortInfo: SortInfo,
        photoMeta: PhotoMeta,
        photoMetaList: MutableLiveData<List<PhotoMeta>>
    ) {
        // XXX Write me.  Make sure you delete the correct entry
        db.collection(rootCollection)
            .document(photoMeta.firestoreID)
            .delete()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "DocumentSnapshot successfully deleted!")
                dbFetchUserMeta(sortInfo, photoMetaList)
            }
            .addOnFailureListener { e -> Log.w(javaClass.simpleName, "Error deleting document", e) }
    }

     */
}