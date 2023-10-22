package com.example.meditrack.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object fBase {
    fun getFireBaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    fun getFireStoreInstance():FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }
    fun getFirebaseStorageInstance():FirebaseStorage{
        return FirebaseStorage.getInstance()
    }
    fun getStorageReference():StorageReference{
        return getFirebaseStorageInstance().reference
    }
    fun getCurrentUser(): FirebaseUser? {
        return getFireBaseAuth().currentUser
    }
    fun getUserReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Users")
    }
    fun getUserEmail(): String? {
        return getCurrentUser()!!.email
    }
    fun getUserId(): String {
        return getCurrentUser()!!.uid
    }

    fun getUserDataQuery(): Query {
        val userRef = getUserReference()
        return userRef.orderByChild("email").equalTo(getUserEmail())
    }
}