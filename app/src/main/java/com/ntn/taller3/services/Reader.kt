package com.ntn.taller3.services

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ntn.taller3.data.InternalLocations
import org.json.JSONArray
import org.json.JSONObject

class Reader(application: Application): AndroidViewModel(application) {
    companion object{
        const val FILE_NAME = "locations.json"
    }
    val internalLocations: List<InternalLocations>
    init {
        internalLocations = retrieveLocations()
    }
    private fun retrieveLocations(): List<InternalLocations> {
        val asset = getApplication<Application>().assets.open(FILE_NAME)
        val json = asset.bufferedReader().use {
            it.readText()
        }
        val obj = JSONObject(json)
        val arr = obj.getJSONArray("locationsArray")
        val locations = mutableListOf<InternalLocations>()
        for(i in 0 until arr.length()){
            val lat = arr.getJSONObject(i).getDouble("latitude")
            val lon = arr.getJSONObject(i).getDouble("longitude")
            val name = arr.getJSONObject(i).getString("name")
            locations.add(InternalLocations(lat, lon, name))
        }
        return locations
    }
}