package com.example.meditrack.homeActivity.about

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutUsViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val teamMemberDetails = MutableLiveData<String>()
    val collegeDetails = MutableLiveData<String>()
    val aboutProjectDetails = MutableLiveData<String>()

    fun setTeamMemberDetailsData(teamMemberDetails: String)
    {
        this.teamMemberDetails.value=teamMemberDetails
    }

    fun setCollegeDetails(collegeDetails: String)
    {
        this.collegeDetails.value=collegeDetails
    }

    fun setAboutProjectDetails(aboutProjectDetails: String)
    {
        this.aboutProjectDetails.value=aboutProjectDetails
    }
}