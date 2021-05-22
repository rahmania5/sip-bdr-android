package com.rahmania.sip_bdr.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.adapter.StudentAdapter
import com.rahmania.sip_bdr.api.ApiClient
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.helper.CustomProgressDialog
import com.rahmania.sip_bdr.helper.SharedPreferences
import com.rahmania.sip_bdr.viewModel.ClassroomScheduleViewModel
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
    private var scheduleVM: ClassroomScheduleViewModel? = null
    private var studentVM: MeetingDetailViewModel? = null
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SharedPreferences? = null
    lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var meetingNumber: String? = null
    private var className: String? = null
    private var sks: String? = null
    private var date: String? = null
    private var startTime: String? = null
    private var finishTime: String? = null
    private var topic: String? = null
    private var presenceStatus: String? = null
    private var tvClassName: TextView? = null
    private var tvSks: TextView? = null
    private var tvSchedule: TextView? = null
    private var tvNumber: TextView? = null
    private var tvDate: TextView? = null
    private var tvTime: TextView? = null
    private var tvTopic: TextView? = null
    private var fabMain: FloatingActionButton? = null
    private var fabEditMeeting: FloatingActionButton? = null
    private var fabDeleteMeeting: FloatingActionButton? = null
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var fabClock: Animation
    private lateinit var fabAntiClock: Animation

    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_detail)
        progressDialog = CustomProgressDialog(this)

        rv = findViewById<View>(R.id.rv_students) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(this)
        tvClassName = findViewById(R.id.tv_classroomName)
        tvSks = findViewById(R.id.tv_sks)
        tvSchedule = findViewById(R.id.tv_schedule)
        tvNumber = findViewById(R.id.tv_meetingNumber)
        tvDate = findViewById(R.id.tv_date)
        tvTime = findViewById(R.id.tv_time)
        tvTopic = findViewById(R.id.tv_topic)

        fabMain = findViewById(R.id.fab)
        fabEditMeeting = findViewById(R.id.fab_editMeeting)
        fabDeleteMeeting = findViewById(R.id.fab_deleteMeeting)

        fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        fabClock = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_clock)
        fabAntiClock = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_anticlock)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user[sessionManager!!.TOKEN]

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

        fabMain?.setOnClickListener {
            isOpen = if (isOpen) {
                fabEditMeeting?.startAnimation(fabClose)
                fabDeleteMeeting?.startAnimation(fabClose)
                fabMain!!.startAnimation(fabAntiClock)
                fabEditMeeting?.isClickable = false
                fabDeleteMeeting?.isClickable = false
                false
            } else {
                fabEditMeeting?.startAnimation(fabOpen)
                fabDeleteMeeting?.startAnimation(fabOpen)
                fabMain!!.startAnimation(fabClock)
                fabEditMeeting?.isClickable = true
                fabDeleteMeeting?.isClickable = true
                true
            }
        }

        try {
            id = intent.extras?.getInt("id")
            val lecturerClassroomId = intent.extras?.getInt("lecturerClassroomId")
            val classroomId = intent.extras?.getInt("classroomId")
            meetingNumber = intent.extras?.getString("meetingNumber")
            date = intent.extras?.getString("date")
            startTime = intent.extras?.getString("startTime")
            finishTime = intent.extras?.getString("finishTime")
            topic = intent.extras?.getString("topic")
            className = intent.extras?.getString("className")
            sks = (intent.extras?.getString("sks") + " SKS")

            val outputDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val inputDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dateFormat = inputDate.parse(date)
            val outputTime = SimpleDateFormat("HH:mm", Locale.US)
            val inputTime = SimpleDateFormat("HH:mm", Locale.US)
            val startTimeFormat = inputTime.parse(startTime)
            val finishTimeFormat = inputTime.parse(finishTime)

            tvClassName!!.text = className
            tvSks!!.text = sks

            scheduleVM = ViewModelProvider(this, NewInstanceFactory())
                .get(ClassroomScheduleViewModel::class.java)
            scheduleVM!!.setClassroomSchedule(token, classroomId!!)
            scheduleVM!!.getClassroomSchedule().observe(this,
                Observer<JSONArray?> { data ->
                    if (data != null && data.length() > 0) {
                        var schedules = ""
                        for (i in 0 until data.length()) {
                            val day = data.getJSONObject(i).getString("scheduled_day")
                            val classStartTime = data.getJSONObject(i).getString("start_time")
                            val classFinishTime = data.getJSONObject(i).getString("finish_time")

                            val classStartTimeFormat = inputTime.parse(classStartTime)
                            val classFinishTimeFormat = inputTime.parse(classFinishTime)

                            schedules = if (i == 0) {
                                schedules + (day + " | " + outputTime.format(classStartTimeFormat) + " - "
                                        + outputTime.format(classFinishTimeFormat))
                            } else {
                                "$schedules\n" + (day + " | " + outputTime.format(classStartTimeFormat) + " - "
                                        + outputTime.format(classFinishTimeFormat))
                            }
                        }
                        tvSchedule!!.text = schedules
                    }
                })

            tvNumber!!.text = ("Pertemuan ke-$meetingNumber")
            tvDate!!.text = outputDate.format(dateFormat)
            tvTime!!.text = (outputTime.format(startTimeFormat) + " - "
                    + outputTime.format(finishTimeFormat))
            tvTopic!!.text = topic

            studentVM = ViewModelProvider(this, NewInstanceFactory()).get(
                MeetingDetailViewModel::class.java
            )
            progressDialog.showLoading()
            studentVM!!.setStudents(token, id)
            studentVM!!.getStudents()?.observe(this,
                Observer<JSONArray?> { data ->
                    if (data != null && data.length() > 0) {
                        studentAdapter.setData(data)
                        fabMain?.visibility = View.GONE
                        fabEditMeeting?.visibility = View.GONE
                        fabDeleteMeeting?.visibility = View.GONE
                    }
                    progressDialog.hideLoading()
                })

            fabEditMeeting?.setOnClickListener { v ->
                when (v.id) {
                    R.id.fab_editMeeting -> {
                        val i = Intent(this@MeetingDetailActivity, EditMeetingActivity::class.java)
                        i.putExtra("id", id!!)
                        i.putExtra("lecturerClassroomId", lecturerClassroomId)
                        i.putExtra("classroomId", classroomId)
                        i.putExtra("meetingNumber", meetingNumber)
                        i.putExtra("date", date)
                        i.putExtra("startTime", startTime)
                        i.putExtra("finishTime", finishTime)
                        i.putExtra("topic", topic)
                        i.putExtra("className", className)
                        i.putExtra("sks", sks)
                        startActivity(i)
                    }
                }
            }

            fabDeleteMeeting?.setOnClickListener { v ->
                when (v.id) {
                    R.id.fab_deleteMeeting -> {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(this@MeetingDetailActivity)
                        builder.setTitle("Hapus Pertemuan")
                        builder.setIcon(R.drawable.ic_delete)
                        builder.setMessage("Anda yakin ingin menghapus pertemuan ini?")

                        builder.setPositiveButton("Ya"
                        ) { dialog, _ ->
                            deleteMeeting(token!!, id!!)
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

    private fun deleteMeeting(token: String, meetingId: Int) {
        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val deleteCall: Call<ResponseBody?>? =
            apiInterface.deleteMeeting(token, meetingId)
        deleteCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.code() == 200) {
                    Toast.makeText(
                        this@MeetingDetailActivity,
                        "Berhasil menghapus pertemuan!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent =
                        Intent(this@MeetingDetailActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    progressDialog.hideLoading()
                } else {
                    Toast.makeText(
                        this@MeetingDetailActivity,
                        "Pertemuan sudah terbentuk. Gagal menghapus pertemuan",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.hideLoading()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
                Toast.makeText(this@MeetingDetailActivity,
                    "Gagal menghapus pertemuuan",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}
