package com.rahmania.sip_bdr.data

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(activity: Activity) {
    val login = "login"
    private val userPref = "MAIN_PREF"
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = activity.getSharedPreferences(userPref, Context.MODE_PRIVATE)
    }

    fun setLoginStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean(login, status).apply()
    }

    fun getLoginStatus():Boolean {
        return sharedPreferences.getBoolean(login, false)
    }
}