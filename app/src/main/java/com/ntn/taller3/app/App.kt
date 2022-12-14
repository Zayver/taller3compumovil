package com.ntn.taller3.app

import android.app.Application
import com.parse.Parse
import com.parse.ParseInstallation




class App : Application() {
    companion object {
        const val PARSE_SERVER = "http://3.80.151.200:1337/parse"
    }

    override fun onCreate() {
        super.onCreate()

        Parse.initialize(
            Parse.Configuration.Builder(this).applicationId("findit")
                .clientKey("finditkey")
                .server(PARSE_SERVER)
                .build()
        )
        //Notifications
        //ParseInstallation.getCurrentInstallation().saveInBackground()

        val installation = ParseInstallation.getCurrentInstallation()
        installation.put("GCMSenderId", "1072832924272")
        val channels: ArrayList<String> = ArrayList()
        channels.add("AvailableUser")
        installation.put("channels", channels)
        installation.saveInBackground()

    }

}