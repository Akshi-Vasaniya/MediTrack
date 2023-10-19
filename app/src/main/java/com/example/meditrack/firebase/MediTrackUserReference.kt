package com.example.meditrack.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

object MediTrackUserReference {
    fun getUserReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Users")
    }
    fun getUserEmail(): String? {
        return MediTrackFirebaseAuth.getCurrentUser()!!.email
    }
    fun getUserId(): String {
        return MediTrackFirebaseAuth.getCurrentUser()!!.uid
    }

    fun getUserDataQuery(): Query {
        val userRef = getUserReference()
        return userRef.orderByChild("email").equalTo(getUserEmail())
    }
}