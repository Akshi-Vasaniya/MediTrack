package com.example.meditrack.mainActivity.login

import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    /*private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> get() = _userData*/

    var inputEmail: String? = null
    var inputPassword: String? = null

    /*private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        // Handle exceptions here
        Log.e("SplashViewModel", "Exception: $exception")
        // You can customize the error message as needed
        val errorMessage = when (exception) {
            is DatabaseException -> "Database Error: ${exception.message}"
            is CancellationException -> "Operation canceled"
            else -> "An error occurred"
        }
        // Use Toast to display the error message (you can replace this with Snackbar if needed)
        val splashFragment = SplashFragment()
        Toast.makeText(
            splashFragment.requireActivity(), // Replace with your application context
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }*/

    /*fun fetchUserData() {
        // Use Kotlin coroutines to fetch data asynchronously
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
                try {
                    userReference.getUserReference()
                        .child(userReference.getUserId())
                        .get().addOnSuccessListener {userSnapshot->
                            if (userSnapshot.exists()) {
                                val name = userSnapshot.child("name").getValue(String::class.java) ?: ""
                                val surname = userSnapshot.child("surname").getValue(String::class.java) ?: ""
                                val email = userSnapshot.child("email").getValue(String::class.java) ?: ""
                                val profileImage = userSnapshot.child("profileImage").getValue(String::class.java) ?: ""
                                val userData = User(name,surname,email, profileImage)
                                _userData.postValue(userData) // Update LiveData in the main thread
                            }
                        }
                } catch (e: Exception) {
                    Log.e("SplashViewModel","$e")
                    // Handle errors here
                }
            }
        }
    }*/
}