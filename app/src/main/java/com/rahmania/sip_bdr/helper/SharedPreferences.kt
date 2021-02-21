package com.rahmania.sip_bdr.helper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.rahmania.sip_bdr.activity.LoginActivity
import com.rahmania.sip_bdr.api.ApiClient.getClient
import com.rahmania.sip_bdr.api.ApiInterface
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object SharedPreferences {
    private var context_: Context? = null
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    val IS_LOGIN = "isLogin"
    val TOKEN = "access_token"
    val NAME = "name"
    val NIP = "nip"

    fun SessionManager(context: Context?): com.rahmania.sip_bdr.helper.SharedPreferences {
        context_ = context
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        editor = sharedPreferences!!.edit()
        return SharedPreferences
    }

    fun createLoginSession(
        token: String,
        name: String?,
        nip: String?
    ) {
        editor!!.putBoolean(
            IS_LOGIN, true)
        editor!!.putString(
            TOKEN, "Bearer $token")
        editor!!.putString(
            NAME, name)
        editor!!.putString(
            NIP, nip)
        editor!!.commit()
    }

    // Storing session
    fun getUserDetail(): HashMap<String, String?>? {
        val user: HashMap<String, String?> = HashMap()
        user[TOKEN] = sharedPreferences!!.getString(
            TOKEN, null)
        user[NAME] = sharedPreferences!!.getString(
            NAME, null)
        user[NIP] = sharedPreferences!!.getString(
            NIP, null)
        return user
    }

    fun logoutSession() {
        editor!!.clear()
        editor!!.commit()
    }

    fun checkToken(): Boolean {
        return sharedPreferences!!.getBoolean(
            IS_LOGIN, false)
    }

    fun isLogin() {
        val token = sharedPreferences!!.getString(
            TOKEN, null)
        if (token != "") {
            val apiInterface: ApiInterface =
                getClient()!!.create(ApiInterface::class.java)
            val isLoginCall: Call<ResponseBody?>? = apiInterface.isLogin(token)
            isLoginCall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>?,
                    response: Response<ResponseBody?>
                ) {
                    if (response.code() != 200) {
                        val intent = Intent(context_, LoginActivity::class.java)
                        logoutSession()
                        context_!!.startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                    Log.e("isLogin", t.message!!)
                }
            })
        }
    }

}