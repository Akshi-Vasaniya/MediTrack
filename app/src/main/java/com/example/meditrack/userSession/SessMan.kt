package com.example.meditrack.userSession

import android.content.Context
import com.example.meditrack.dataModel.dataClasses.SessData
import com.example.meditrack.dataModel.enumClasses.others.SessStatus
import com.example.meditrack.firebase.FBase
import com.example.meditrack.utility.UtilsFunctions.Companion.getDeviceInformation
import kotlinx.coroutines.*

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
                                    .addOnSuccessListener { callback(false) }
                                    .addOnFailureListener { callback(false) }
                            }
                        } else { callback(false) }
                    } else { callback(false) }
                }
                .addOnFailureListener { callback(false) }
        }
        catch (ex:java.lang.Exception){ callback(false) }

    }

    fun createSession(callback: (String?) -> Unit){
        try{
            runBlocking {
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
                        throw e
                    }
            }
        }
        catch (ex:java.lang.Exception){
            throw ex
        }
    }
    private suspend fun collectSessionData(): SessData {
        return withContext(Dispatchers.IO){
            var deviseInfo: HashMap<String, String>? = null
            var locInfo: HashMap<String, String>? = null
            val job1 = launch { deviseInfo = findDeviceInfo() }
            val job2 = launch{ locInfo = findLocInfo()}
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
            val loginTimeStamp = async { SessionUtils.getLoginTimestamp() }
            val logoutTimeStamp = "null"
            val expireTimeStamp = async { SessionUtils.getSevenDaysExpiryTimestamp() }
            val sessStatus = SessStatus.LOGGED_IN
            job1.join()
            job2.join()

            val job3 = launch {
                deviseInfo?.let {
                    deviceID = it.getOrDefault("deviceId","null")
                    deviceName = it.getOrDefault("deviceName","null")
                    deviceType = it.getOrDefault("deviceType","null")
                    osVersion = it.getOrDefault("osVersion","null")
                    appVersion = it.getOrDefault("appVersion","null")
                    apiLevel = it.getOrDefault("apiLevel","null")
                }
            }

            val job4 = launch {
                locInfo?.let{
                    locationCountry = it.getOrDefault("country","null")
                    locationCity = it.getOrDefault("city","null")
                    locationState = it.getOrDefault("state","null")
                    locationArea = it.getOrDefault("area","null")
                }
            }

            job3.join()
            job4.join()

            SessData(
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
                loginTimestamp = loginTimeStamp.await(),
                logoutTimestamp = logoutTimeStamp,
                status = sessStatus,
                expiryTimestamp = expireTimeStamp.await()
            )

        }

    }



    private fun findDeviceInfo(): HashMap<String, String>? {
        return try {
            val deviceInfo = mycontext.getDeviceInformation()
            deviceInfo
        } catch (ex:java.lang.Exception){
            null
        }
    }

    private fun findLocInfo(): HashMap<String, String>? {
        return try {
            val loc = locationUtils.getLastKnownLocation()
            if(loc!=null) {
                locationUtils.getLocationInfo(loc.latitude, loc.longitude)
            } else{
                null
            }

        } catch (ex:Exception){
            null
        }

    }
}