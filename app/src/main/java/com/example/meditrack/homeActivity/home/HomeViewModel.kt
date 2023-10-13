package com.example.meditrack.homeActivity.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meditrack.dataModel.User

class HomeViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    val _userData = MutableLiveData<User>()

    fun setUserData(user: User)
    {
        _userData.value=user
    }
}