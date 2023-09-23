package com.example.meditrack.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

object userReference {
    fun getUserReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Users")
    }
    fun getUserEmail(): String? {
        return firebaseAuth.getCurrentUser()!!.email
    }
    fun getUserId(): String {
        return firebaseAuth.getCurrentUser()!!.uid
    }

    fun getUserDataQuery(): Query {
        val userRef = getUserReference()
        return userRef.orderByChild("email").equalTo(getUserEmail())
    }
}