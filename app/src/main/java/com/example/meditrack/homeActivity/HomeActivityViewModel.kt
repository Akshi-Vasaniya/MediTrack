package com.example.meditrack.homeActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meditrack.dataModel.User

class HomeActivityViewModel : ViewModel(){
    val _userData = MutableLiveData<User>()

    fun setUserData(user: User)
    {
        _userData.value=user
    }
}