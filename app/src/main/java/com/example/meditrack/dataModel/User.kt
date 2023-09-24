package com.example.meditrack.dataModel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.meditrack.R
import com.example.meditrack.exception.handleException
import com.example.meditrack.firebase.userReference
import com.example.meditrack.utility.utilityFunction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

            Log.i("Profile Image", profileImage.toString())
            return User(name,surname,email,profileImage)
        }

        suspend fun fetchUserData(context: Context,dataSnapshot: DataSnapshot,TAG:String)
        {
            withContext(Dispatchers.IO){
                val userData = mapDataSnapshotToUser(dataSnapshot)

                val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("name", userData.name)
                editor.putString("surname", userData.surname)
                editor.putString("email", userData.email)


                if(userData.profileImage != "null")
                {
                    editor.putBoolean("hasImage",true)

                    Log.i(TAG, userData.profileImage.toString())
                    val bitmap = utilityFunction.decodeBase64ToBitmap(userData.profileImage.toString())

                    val cacheDirectory = context.cacheDir
                    Log.i(TAG, cacheDirectory.absolutePath)

                    val imageFileName = "profileImg.jpg"
                    Log.i(TAG, imageFileName)

                    val imageFile = File(cacheDirectory, imageFileName)
                    Log.i(TAG,imageFile.toString())
                    val outputStream = FileOutputStream(imageFile)
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    /*MainScope().launch(Dispatchers.Main) {
                        val activity = requireActivity() as HomeActivity
                        val parentView = activity.myToolbarImage.parent as View
                        activity.myToolbarImage.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        parentView.requestLayout()
                        activity.myToolbarImage.setImageBitmap(bitmap)
                    }*/
                }
                else{
                    editor.putBoolean("hasImage",false)
                }
                editor.apply()
            }
        }

        fun isUsernameAvailable(context: Context,TAG:String,username: String, callback: (Boolean) -> Unit) {
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
        }
    }

}
