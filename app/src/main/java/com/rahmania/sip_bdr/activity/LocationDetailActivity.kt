package com.rahmania.sip_bdr.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.api.ApiClient
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.helper.CustomProgressDialog
import com.rahmania.sip_bdr.helper.SharedPreferences
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class LocationDetailActivity : AppCompatActivity() {
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SharedPreferences? = null
    lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var tvName: TextView? = null
    private var tvNim:TextView? = null
    private var tvAddress:TextView? = null
    private var tvLongitude:TextView? = null
    private var tvLatitude:TextView? = null
    private var tvStatus:TextView? = null
    private var btnAccept: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_detail)
        progressDialog = CustomProgressDialog(this)

        tvName = findViewById(R.id.tv_name)
        tvNim = findViewById(R.id.tv_nim)
        tvAddress = findViewById(R.id.tv_address)
        tvLongitude = findViewById(R.id.tv_longitude)
        tvLatitude = findViewById(R.id.tv_latitude)
        tvStatus = findViewById(R.id.tv_submission_status)

        btnAccept = findViewById(R.id.btn_accept)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

        try {
            val intent = intent
            val locationDetail = JSONObject(intent.getStringExtra("studentlocation"))
            id = locationDetail.getInt("id")
            tvName!!.text = locationDetail.getString("name")
            tvNim!!.text = locationDetail.getString("nim")
            tvAddress!!.text = locationDetail.getString("address")
            tvLongitude!!.text = locationDetail.getString("longitude")
            tvLatitude!!.text = locationDetail.getString("latitude")
            tvStatus!!.text = locationDetail.getString("submission_status")

            btnAccept?.setOnClickListener { v ->
                when (v.id) {
                    R.id.btn_accept -> {
                        acceptSubmission(token!!, id!!)
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun acceptSubmission(token: String, id: Int) {
        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val acceptLocationCall: Call<ResponseBody?>? = apiInterface.acceptSubmission(token, id,
            "Disetujui"
        )
        acceptLocationCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                try {
                    Toast.makeText(
                        this@LocationDetailActivity,
                        "Berhasil menyetujui pengajuan lokasi!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent =
                        Intent(this@LocationDetailActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                progressDialog.hideLoading()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
            }
        })
    }
}
