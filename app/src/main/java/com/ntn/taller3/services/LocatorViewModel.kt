package com.ntn.taller3.services

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class LocatorViewModel(application: Application) : AndroidViewModel(application) {

    private val locationData = Locator(application)
    fun requestLocationUpdates() = locationData
}