package com.ntn.taller3.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class Locator(context: Context) : LiveData<LatLng>() {


    companion object {
        private const val SHORT_GPS_UPDATE_INTERVAL = 500L
        private const val LONG_GPS_UPDATE_INTERVAL = 1000L

        val locationRequest = LocationRequest.create()
            .setInterval(LONG_GPS_UPDATE_INTERVAL)
            .setFastestInterval(SHORT_GPS_UPDATE_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
    }


    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let { setLocationData(it) }
        }
    }


    override fun onActive() {
        super.onActive()
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun setLocationData(location: Location) {
        location.let {
            value = LatLng(location.latitude, location.longitude)
        }
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}