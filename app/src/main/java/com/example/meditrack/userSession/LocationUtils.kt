package com.example.meditrack.userSession

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*


class LocationUtils(private val context: Context) {

    // Get the last known location
    fun getLastKnownLocation(): Location? {
        // Check for location permissions
        if (isLocationPermissionGranted()) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers: List<String> = locationManager.getProviders(true)

                var bestLocation: Location? = null

                for (provider in providers) {
                    try {
                        val location = locationManager.getLastKnownLocation(provider)
                        if (location != null && (bestLocation == null || location.accuracy < bestLocation.accuracy)) {
                            bestLocation = location
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }

                return bestLocation
            }
            catch (ex:Exception){
                return null
            }

        } else {
            // Handle case where location permission is not granted
            // You might want to request permission or notify the user
            return null
        }
    }

    // Get location information (country, city, area) based on latitude and longitude
    fun getLocationInfo(latitude: Double, longitude: Double): HashMap<String, String>? {
        // Check for location permissions
        if (isLocationPermissionGranted()) {
            try {
                val locationInfo = HashMap<String, String>()
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses!!.isNotEmpty()) {
                    val address = addresses[0]

                    // Country
                    locationInfo["country"] = address.countryName

                    // City
                    locationInfo["city"] = address.locality

                    //State
                    locationInfo["state"] = address.adminArea

                    // Area or Region
                    locationInfo["area"] = address.subLocality ?: ""
                }
                return locationInfo
            } catch (e: IOException) {
                e.printStackTrace()
                return  null
            }
        } else {
            // Handle case where location permission is not granted
            // You might want to request permission or notify the user
            return null
        }
    }

    // Check if the location permission is granted
    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

}
