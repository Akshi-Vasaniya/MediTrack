package com.example.meditrack.dataModel

data class User(var name:String?=null, var surname:String?=null, var email:String?=null, var profileImage:String?=null){
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
    }

}
