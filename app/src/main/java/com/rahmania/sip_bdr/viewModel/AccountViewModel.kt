package com.rahmania.sip_bdr.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rahmania.sip_bdr.api.ApiClient.getClient
import com.rahmania.sip_bdr.api.ApiInterface
import okhttp3.ResponseBody
import kotlin.collections.HashMap
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class AccountViewModel : ViewModel() {
    private lateinit var apiInterface: ApiInterface
    private val userDetails = MutableLiveData<HashMap<String, String>>()
    val name = "name"
    val nip = "nip"

    fun setProfile(token: String?) {
        val userProfile: HashMap<String, String> = HashMap()
        this.apiInterface = getClient()!!.create(ApiInterface::class.java)
        apiInterface.getDetails(token)?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful) {
                    val jsonRESULTS: JSONObject?
                    try {
                        jsonRESULTS = JSONObject(response.body()!!.string())
                        val profile = jsonRESULTS.getJSONObject("data")
                        if (profile.length() != 0) {
                            val userName =
                                profile.getJSONObject("user").getString("name")
                            val userNip =
                                profile.getJSONObject("user").getString("nip")

                            userProfile[name] = userName
                            userProfile[nip] = userNip
                            userDetails.value = userProfile
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                Log.e("Error by view model", t.message.toString())
            }
        })
    }

    fun getProfile(): LiveData<HashMap<String, String>> {
        return userDetails
    }

}