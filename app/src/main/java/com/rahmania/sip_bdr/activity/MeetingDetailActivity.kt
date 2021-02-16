package com.rahmania.sip_bdr

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rahmania.sip_bdr.adapter.StudentAdapter
import com.rahmania.sip_bdr.api.ApiClient
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.api.SessionManager
import com.rahmania.sip_bdr.viewModel.MeetingDetailViewModel
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MeetingDetailActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    private var studentVM: MeetingDetailViewModel? = null
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SessionManager? = null
    lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var meetingNumber: String? = null
    private var date: String? = null
    private var startTime: String? = null
    private var finishTime: String? = null
    private var presenceStatus: String? = null
    private var tvName: TextView? = null
    private var tvNim:TextView? = null
    private var tvStatus:TextView? = null
    private var tvNumber:TextView? = null
    private var tvDate:TextView? = null
    private var tvTime:TextView? = null
    private var fabEditMeeting: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_detail)
        progressDialog = CustomProgressDialog(this)

        rv = findViewById<View>(R.id.rv_students) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(this)
        tvName = findViewById(R.id.tv_name)
        tvNim = findViewById(R.id.tv_nim)
        tvStatus = findViewById(R.id.tv_presence_status)
        tvNumber = findViewById(R.id.tv_meetingNumber)
        tvDate = findViewById(R.id.tv_date)
        tvTime = findViewById(R.id.tv_time)

        fabEditMeeting = findViewById(R.id.fab_editMeeting)

        sessionManager = SessionManager.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

        val studentAdapter = StudentAdapter()
        studentAdapter.StudentAdapter(object: StudentAdapter.OnItemClickListener {
            @Throws(JSONException::class)
            override fun onItemClick(item: JSONObject) {
                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(this@MeetingDetailActivity)
                builder.setTitle("Edit Status Kehadiran")
                builder.setIcon(R.drawable.ic_checked)
                val choices = arrayOf("Hadir", "Izin", "Absen")
                val checkedItem: Int = when {
                    item.getString("presence_status") == "Hadir" -> {
                        0
                    }
                    item.getString("presence_status") == "Izin" -> {
                        1
                    }
                    else -> {
                        2
                    }
                }

                builder.setSingleChoiceItems(
                    choices,
                    checkedItem
                ) { _, _ -> }

                builder.setPositiveButton("OK"
                ) { dialog, _ ->
                    try {
                        val selectedStatus =
                            (dialog as AlertDialog).listView.checkedItemPosition
                        presenceStatus = when (selectedStatus) {
                            0 -> {
                                "Hadir"
                            }
                            1 -> {
                                "Izin"
                            }
                            else -> {
                                "Absen"
                            }
                        }
                        val attendanceId = item.getInt("id")
                        editAttendanceStatus(token!!, attendanceId, presenceStatus!!)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                builder.setNegativeButton("Cancel", null)
                val dialog: AlertDialog? = builder.create()
                dialog?.setCanceledOnTouchOutside(false)
                dialog?.show()
            }
        })
        studentAdapter.notifyDataSetChanged()
        rv!!.adapter = studentAdapter

        try {
            id = intent.extras?.getInt("id")
            meetingNumber = intent.extras?.getString("meetingNumber")
            date = intent.extras?.getString("date")
            startTime = intent.extras?.getString("startTime")
            finishTime = intent.extras?.getString("finishTime")

            val outputDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val inputDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dateFormat = inputDate.parse(date)
            val outputTime = SimpleDateFormat("HH:mm", Locale.US)
            val inputTime = SimpleDateFormat("HH:mm", Locale.US)
            val startTimeFormat = inputTime.parse(startTime)
            val finishTimeFormat = inputTime.parse(finishTime)

            tvNumber!!.text = ("Pertemuan ke-$meetingNumber")
            tvDate!!.text = outputDate.format(dateFormat)
            tvTime!!.text = (outputTime.format(startTimeFormat) + " - "
                    + outputTime.format(finishTimeFormat))

            studentVM = ViewModelProvider(this, NewInstanceFactory()).get(
                MeetingDetailViewModel::class.java
            )
            progressDialog.showLoading()
            studentVM!!.setStudents(token, id)
            studentVM!!.getStudents()?.observe(this,
                Observer<JSONArray?> { data ->
                    if (data != null) {
                        studentAdapter.setData(data)
                        progressDialog.hideLoading()
                    }
                })

            fabEditMeeting?.setOnClickListener { v ->
                when (v.id) {
                    R.id.fab_editMeeting -> {
                        val i = Intent(this@MeetingDetailActivity, EditMeetingActivity::class.java)
                        i.putExtra("id", id!!)
                        i.putExtra("meetingNumber", meetingNumber!!)
                        i.putExtra("date", date!!)
                        i.putExtra("startTime", startTime!!)
                        i.putExtra("finishTime", finishTime!!)
                        startActivity(i)
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun editAttendanceStatus(token: String, attendanceId: Int, presenceStatus: String) {
        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val editStatusCall: Call<ResponseBody?>? =
            apiInterface.editAttendanceStatus(token, attendanceId, presenceStatus)
        editStatusCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Toast.makeText(this@MeetingDetailActivity, "Berhasil memperbarui status kehadiran!", Toast.LENGTH_SHORT).show()
                studentVM?.setStudents(token, id)
                progressDialog.hideLoading()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
                Toast.makeText(this@MeetingDetailActivity,
                    "Gagal memperbarui status kehadiran",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}
