package com.rahmania.sip_bdr.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
class LocationDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SharedPreferences? = null
    lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private lateinit var longitude: String
    private lateinit var latitude: String
    private var tvName: TextView? = null
    private var tvNim:TextView? = null
    private var tvAddress:TextView? = null
    private var tvLongitude:TextView? = null
    private var tvLatitude:TextView? = null
    private var tvStatus:TextView? = null
    private var btnAccept: Button? = null
    private var btnDecline: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_detail)
        progressDialog = CustomProgressDialog(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        tvName = findViewById(R.id.tv_name)
        tvNim = findViewById(R.id.tv_nim)
        tvAddress = findViewById(R.id.tv_address)
        tvLongitude = findViewById(R.id.tv_longitude)
        tvLatitude = findViewById(R.id.tv_latitude)
        tvStatus = findViewById(R.id.tv_submission_status)

        btnAccept = findViewById(R.id.btn_accept)
        btnDecline = findViewById(R.id.btn_decline)

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

            val submissionStatus = locationDetail.getString("submission_status")
            if(submissionStatus != "Belum Disetujui") {
                btnAccept!!.visibility = View.GONE
                btnDecline!!.visibility = View.GONE
            }

            btnAccept?.setOnClickListener { v ->
                when (v.id) {
                    R.id.btn_accept -> {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(this@LocationDetailActivity)
                        builder.setTitle("Terima Pengajuan Lokasi")
                        builder.setIcon(R.drawable.ic_checked)
                        builder.setMessage("Anda yakin ingin menyetujui pengajuan ini?")

                        builder.setPositiveButton("Ya"
                        ) { dialog, _ ->
                            acceptSubmission(token!!, id!!)
                        }

                        builder.setNegativeButton("Cancel", null)
                        val dialog: AlertDialog? = builder.create()
                        dialog?.setCanceledOnTouchOutside(false)
                        dialog?.show()
                    }
                }
            }

            btnDecline?.setOnClickListener { v ->
                when (v.id) {
                    R.id.btn_decline -> {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(this@LocationDetailActivity)
                        builder.setTitle("Tolak Pengajuan Lokasi")
                        builder.setIcon(R.drawable.ic_delete)
                        builder.setMessage("Anda yakin ingin menolak pengajuan ini?")

                        builder.setPositiveButton("Ya"
                        ) { dialog, _ ->
                            declineSubmission(token!!, id!!)
                        }

                        builder.setNegativeButton("Cancel", null)
                        val dialog: AlertDialog? = builder.create()
                        dialog?.setCanceledOnTouchOutside(false)
                        dialog?.show()
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val intent = intent
        val locationDetail = JSONObject(intent.getStringExtra("studentlocation"))
        longitude = locationDetail.getString("longitude")
        latitude = locationDetail.getString("latitude")

        googleMap?.apply {
            val location = LatLng(latitude.toDouble(), longitude.toDouble())
            addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Student's Location")
            )
            moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0F))
        }
    }

    private fun acceptSubmission(token: String, id: Int) {
        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val acceptLocationCall: Call<ResponseBody?>? = apiInterface.followUpSubmission(token, id,
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

    private fun declineSubmission(token: String, id: Int) {
        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val acceptLocationCall: Call<ResponseBody?>? = apiInterface.followUpSubmission(token, id,
            "Ditolak"
        )
        acceptLocationCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                try {
                    Toast.makeText(
                        this@LocationDetailActivity,
                        "Berhasil menolak pengajuan lokasi!",
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
