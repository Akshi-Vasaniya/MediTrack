package com.example.meditrack.homeActivity.about

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meditrack.dataModel.dataClasses.UserData

class AboutUsViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val _teamMemberDetails = MutableLiveData<String>()
    val _collegeDetails = MutableLiveData<String>()
    val _aboutProjectDetails = MutableLiveData<String>()

    fun setTeamMemberDetailsData(teamMemberDetails: String)
    {
        _teamMemberDetails.value=teamMemberDetails
    }

    fun setCollegeDetails(collegeDetails: String)
    {
        _collegeDetails.value=collegeDetails
    }

    fun setAboutProjectDetails(aboutProjectDetails: String)
    {
        _aboutProjectDetails.value=aboutProjectDetails
    }
}