package com.example.meditrack.dataModel.dataClasses

import com.example.meditrack.dataModel.enumClasses.others.SessionStatus

data class UserSessionData(
    val deviceId: String?="null",
    val deviceName: String?="null",
    val deviceType: String?="null",
    val osVersion: String?="null",
    val appVersion: String?="null",
    val apiLevel: String?="null",
    val country: String?="null",
    val state:String?="null",
    val city: String?="null",
    val area: String?="null",
    val loginTimestamp: String,
    val logoutTimestamp: String?="null",
    var status: SessionStatus,
    val expiryTimestamp: String
)
