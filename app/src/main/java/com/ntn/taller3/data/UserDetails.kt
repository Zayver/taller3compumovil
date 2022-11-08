package com.ntn.taller3.data

import android.graphics.Bitmap

data class UserDetails(
    val username: String,
    val image: Bitmap?,
    val latitude: Double,
    val longitude: Double,
    var objectID: String,
    ){

}