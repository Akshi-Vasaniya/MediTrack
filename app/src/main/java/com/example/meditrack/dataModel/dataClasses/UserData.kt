package com.example.meditrack.dataModel.dataClasses

import com.example.meditrack.dataModel.EmailAvailabilityCallback
import com.example.meditrack.firebase.FBase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

data class UserData(var name:String?=null, var surname:String?=null, var email:String?=null, var profileImage:String?=null){
    companion object{

        /*fun isUsernameAvailable(context: Context,TAG:String,username: String, callback: (Boolean) -> Unit) {
            val usersRef = userReference.getUserReference()

            // Query the database for the provided username
            usersRef.orderByChild("name").equalTo(username).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // If no user has this username, it's available
                    val isAvailable = dataSnapshot.childrenCount == 0L
                    callback(isAvailable)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    handleException.firebaseDatabaseExceptions(context,databaseError,TAG)
                    callback(false) // Assume the username is not available on error
                }
            })
        }*/

        fun isEmailAvailable(email: String, callback: EmailAvailabilityCallback){
            val userRefForEmail = FBase.getUserReference()

            userRefForEmail.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener{
                override fun onDataChange(emailSnapshot: DataSnapshot) {
                    val isAvailable = emailSnapshot.childrenCount == 0L
                    callback.onResult(!isAvailable)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    callback.onResult(true)
                }
            })
        }
    }

}
