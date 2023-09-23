package com.example.meditrack.dataModel

import android.util.Log
import com.example.meditrack.firebase.userReference
import com.google.firebase.database.DataSnapshot

data class User(var name:String?=null, var surname:String?=null, var email:String?=null, var profileImage:String?=null){
    companion object{
        fun mapDataSnapshotToUser(snapshot: DataSnapshot): User {
            var name:String?=null
            var surname:String?=null
            var email:String?=null
            var profileImage:String?=null
            snapshot.children.forEach {

                name=it.child("name").value.toString()
                surname=it.child("surname").value.toString()
                email=it.child("email").value.toString()
                profileImage=it.child("profileImage").value.toString()
            }

            return User(name,surname,email,profileImage)
        }
    }

}
