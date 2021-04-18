package com.rahmania.sip_bdr.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rahmania.sip_bdr.api.ApiClient.getClient
import com.rahmania.sip_bdr.api.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class MeetingNumberViewModel : ViewModel() {
    private lateinit var apiInterface: ApiInterface
    private val meetingNumber = MutableLiveData<JSONArray>()

    fun setMeetingNumber(token: String?, lecturerClassroomId: Int?) {
        this.apiInterface = getClient()!!.create(ApiInterface::class.java)
        apiInterface.getMeetingNumbers(token, lecturerClassroomId)?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful) {
                    val jsonRESULTS: JSONObject?
                    try {
                        jsonRESULTS = JSONObject(response.body()!!.string())
                        val meetingNo = jsonRESULTS.getJSONArray("meeting_numbers")
                        meetingNumber.postValue(meetingNo)
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

    fun getMeetingNumber(): LiveData<JSONArray>? {
        return meetingNumber
    }
}