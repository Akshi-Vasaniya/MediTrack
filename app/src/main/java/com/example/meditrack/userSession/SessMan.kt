package com.example.meditrack.userSession

import android.content.Context
import android.util.Log
import com.example.meditrack.dataModel.dataClasses.SessData
import com.example.meditrack.dataModel.enumClasses.others.SessStatus
import com.example.meditrack.firebase.FBase
import com.example.meditrack.utility.UtilityFunction

class SessMan(val mycontext: Context, private var locationUtils: LocationUtils = LocationUtils(mycontext)) {


    fun checkSession(sessionId: String, callback: (Boolean) -> Unit) {
        try {
            val sessionDocRef = FBase.getUsersSessionsDataCollection()
            sessionDocRef.document(sessionId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val status = documentSnapshot.getString("status")
                        val expiryTimestamp = documentSnapshot.getString("expiryTimestamp")

                        if (status == SessStatus.LOGGED_IN.name) {
                            if(!SessionUtils.isSessionExpired(expiryTimestamp!!))
                            {
                                callback(true)
                            }
                            else{
                                val updates = mapOf(
                                    "status" to SessStatus.LOGGED_OUT.name,
                                    "logoutTimestamp" to SessionUtils.getLogoutTimestamp()
                                )
                                sessionDocRef.document(sessionId).update(updates)
                                    .addOnSuccessListener {
                                        callback(false)
                                    }
                                    .addOnFailureListener { e ->
                                        callback(false)
                                    }
                            }
                        } else {
                            callback(false)
                        }
                    } else {
                        callback(false)
                    }
                }
                .addOnFailureListener { e ->
                    callback(false)
                }
        }
        catch (ex:java.lang.Exception){
            callback(false)
        }

    }



    fun createSession(callback: (String?) -> Unit){
        try{
            val sessionData = collectSessionData()
            val collectionReference = FBase.getUsersSessionsDataCollection()
            collectionReference.add(sessionData)
                .addOnSuccessListener { documentReference ->
                    // Document added successfully
                    val documentId = documentReference.id
                    LocalSession.createSession(mycontext, documentId)
                    // Use the documentId as needed
                    callback(documentId)
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    callback(null)
                }
        }
        catch (ex:java.lang.Exception){
            callback(null)
        }
    }
    private fun collectSessionData(): SessData {
        val deviseInfo = findDeviceInfo()
        val locInfo = findLocInfo()
        var deviceID ="null"
        var deviceName = "null"
        var deviceType = "null"
        var osVersion = "null"
        var appVersion = "null"
        var apiLevel = "null"
        var locationCountry = "null"
        var locationCity = "null"
        var locationState = "null"
        var locationArea = "null"
        if(deviseInfo!=null)
        {
            deviceID = deviseInfo.getOrDefault("deviceId","null")
            deviceName = deviseInfo.getOrDefault("deviceName","null")
            deviceType = deviseInfo.getOrDefault("deviceType","null")
            osVersion = deviseInfo.getOrDefault("osVersion","null")
            appVersion = deviseInfo.getOrDefault("appVersion","null")
            apiLevel = deviseInfo.getOrDefault("apiLevel","null")
        }
        if(locInfo!=null){
            locationCountry = locInfo.getOrDefault("country","null")
            locationCity = locInfo.getOrDefault("city","null")
            locationState = locInfo.getOrDefault("state","null")
            locationArea = locInfo.getOrDefault("area","null")
        }

        val loginTimeStamp = SessionUtils.getLoginTimestamp()
        val logoutTimeStamp = "null"
        val expireTimeStamp = SessionUtils.getSevenDaysExpiryTimestamp()
        val sessStatus = SessStatus.LOGGED_IN

        val sessData = SessData(
            deviceId = deviceID,
            deviceName = deviceName,
            deviceType = deviceType,
            osVersion = osVersion,
            appVersion = appVersion,
            apiLevel = apiLevel,
            country = locationCountry,
            state = locationState,
            city = locationCity,
            area = locationArea,
            loginTimestamp = loginTimeStamp,
            logoutTimestamp = logoutTimeStamp,
            status = sessStatus,
            expiryTimestamp = expireTimeStamp
        )
        return sessData
    }



    private fun findDeviceInfo(): HashMap<String, String>? {
        return try {
            val deviceInfo = UtilityFunction.getDeviceInformation(mycontext)
            if(deviceInfo!=null){
                Log.i("Device Info",deviceInfo.toString())
            } else{
                Log.i("Device Info","null")
            }
            deviceInfo
        } catch (ex:java.lang.Exception){
            null
        }
    }

    private fun findLocInfo(): HashMap<String, String>? {
        try {
            val loc = locationUtils.getLastKnownLocation()
            if(loc!=null)
            {
                Log.i("Locations Info 1",loc.toString())
                val locResults = locationUtils.getLocationInfo(loc.latitude,loc.longitude)
                return if(locResults!=null){
                    Log.i("Locations Info 2",locResults.toString())
                    locResults
                } else{
                    Log.i("Locations Info 2","null")
                    null
                }
            }
            else{
                Log.i("Locations Info 1","null")
                return null
            }

        }
        catch (ex:Exception){
            return null
        }

    }
}