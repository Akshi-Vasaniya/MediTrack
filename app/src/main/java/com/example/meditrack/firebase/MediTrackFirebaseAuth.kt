package com.example.meditrack.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object MediTrackFirebaseAuth {
    fun getFireBaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    fun getCurrentUser(): FirebaseUser? {
        return getFireBaseAuth().currentUser
    }
}