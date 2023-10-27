package com.example.meditrack.homeActivity.userprofile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meditrack.dataModel.dataClasses.UserData

class UserProfileViewModel : ViewModel() {
    val _userData = MutableLiveData<UserData>()

    fun setUserData(user: UserData)
    {
        _userData.value=user
    }
}