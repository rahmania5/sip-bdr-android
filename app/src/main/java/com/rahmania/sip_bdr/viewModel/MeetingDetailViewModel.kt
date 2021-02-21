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


class MeetingDetailViewModel : ViewModel() {
    private lateinit var apiInterface: ApiInterface
    private val studentsData = MutableLiveData<JSONArray>()

    fun setStudents(token: String?, meetingId: Int?) {
        this.apiInterface = getClient()!!.create(ApiInterface::class.java)
        apiInterface.getStudentAttendances(token, meetingId)?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful) {
                    val jsonRESULTS: JSONObject?
                    try {
                        jsonRESULTS = JSONObject(response.body()!!.string())
                        val students = jsonRESULTS.getJSONArray("studentattendance")
                        studentsData.postValue(students)
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

    fun getStudents(): LiveData<JSONArray>? {
        return studentsData
    }
}