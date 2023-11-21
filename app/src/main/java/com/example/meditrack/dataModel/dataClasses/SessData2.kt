package com.example.meditrack.dataModel.dataClasses

import com.example.meditrack.dataModel.enumClasses.others.SessStatus

data class SessData2(
    val sessionId:String,
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
    var status: SessStatus,
    val expiryTimestamp: String
)
{
    companion object{
        fun Map<String, Any>.toUserSessionData(sId:String): SessData2 {
            return SessData2(
                sessionId = sId,
                deviceId = this["deviceId"] as? String,
                deviceName = this["deviceName"] as? String,
                deviceType = this["deviceType"] as? String,
                osVersion = this["osVersion"] as? String,
                appVersion = this["appVersion"] as? String,
                apiLevel = this["apiLevel"] as? String,
                country = this["country"] as? String,
                state = this["state"] as? String,
                city = this["city"] as? String,
                area = this["area"] as? String,
                loginTimestamp = this["loginTimestamp"] as String,
                logoutTimestamp = this["logoutTimestamp"] as? String,
                status = SessStatus.valueOf(this["status"] as String),
                expiryTimestamp = this["expiryTimestamp"] as String
            )
        }
    }
}
