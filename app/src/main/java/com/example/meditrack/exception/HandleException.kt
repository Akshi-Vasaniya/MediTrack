package com.example.meditrack.exception

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

class HandleException {
    companion object{
        fun firebaseCommonExceptions(context: Context,exception: Exception,TAG:String){
            when (exception) {
                is FirebaseNetworkException ->{
                    Toast.makeText(context,"No Network Connection", Toast.LENGTH_SHORT).show()
                }
                is FirebaseTooManyRequestsException ->{
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,"Too many request, Try after some time", Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthInvalidUserException -> {
                    // Handle invalid user exception
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,"Invalid user", Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthUserCollisionException -> {
                    // Handle email already exists exception
                    Log.i(TAG,exception.toString())
                    Toast.makeText(context,"Email already exists",Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthWeakPasswordException -> {
                    // Handle weak password exception
                    Log.i(TAG,exception.toString())
                    Toast.makeText(context,"Your Password is too weak",Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthEmailException -> {
                    // Handle invalid email exception
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,"Invalid email", Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    // Handle invalid email or password exception
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,"Invalid email and password", Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthRecentLoginRequiredException -> {
                    // Handle recent login required exception
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,"Login required", Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthActionCodeException -> {
                    // Handle action code exception
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,exception.toString(), Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthWebException -> {
                    // Handle web exception
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,exception.toString(), Toast.LENGTH_SHORT).show()
                }
                is FirebaseAuthMultiFactorException -> {
                    // Handle multi-factor exception
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,exception.toString(), Toast.LENGTH_SHORT).show()
                }
                is FirebaseException -> {
                    val errorCode = exception.message
                    if (errorCode == "An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]") {
                        // Handle the specific error for invalid login credentials here
                        Toast.makeText(context,"Invalid Credentials", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle other FirebaseAuthException errors
                        // You can log or display a generic error message
                        Toast.makeText(context,exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    // Handle other exceptions
                    Log.e(TAG,exception.toString())
                    Toast.makeText(context,exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        /*fun firebaseDatabaseExceptions(context: Context,databaseError: DatabaseError,TAG:String){
            when (databaseError.code) {
                DatabaseError.PERMISSION_DENIED -> {
                    // Permission denied (e.g., write operation denied by security rules)
                    Log.e(TAG,databaseError.message)
                    Toast.makeText(context,"Permission Denied", Toast.LENGTH_SHORT).show()
                }
                DatabaseError.DISCONNECTED -> {
                    // The operation was interrupted due to a network disconnect
                    Log.e(TAG,databaseError.message)
                    Toast.makeText(context,"Disconnected", Toast.LENGTH_SHORT).show()
                }
                DatabaseError.EXPIRED_TOKEN -> {
                    // The authentication token has expired
                    Log.e(TAG,databaseError.message)
                    Toast.makeText(context,"Expired Token", Toast.LENGTH_SHORT).show()
                }
                DatabaseError.INVALID_TOKEN -> {
                    // The provided authentication token is invalid
                    Log.e(TAG,databaseError.message)
                    Toast.makeText(context,"Invalid Token", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Handle other error codes or use error.message to get the error message
                    Log.e(TAG,databaseError.message)
                    Toast.makeText(context,databaseError.message, Toast.LENGTH_SHORT).show()
                    // Handle the error accordingly
                }
            }
        }*/
    }
}