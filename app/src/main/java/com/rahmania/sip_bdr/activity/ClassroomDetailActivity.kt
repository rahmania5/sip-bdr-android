package com.rahmania.sip_bdr.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.adapter.MeetingAdapter
import com.rahmania.sip_bdr.helper.CustomProgressDialog
import com.rahmania.sip_bdr.helper.SharedPreferences
import com.rahmania.sip_bdr.viewModel.ClassroomDetailViewModel
import com.rahmania.sip_bdr.viewModel.ClassroomScheduleViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ClassroomDetailActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    private var scheduleVM: ClassroomScheduleViewModel? = null
    private var meetingVM: ClassroomDetailViewModel? = null
    private var sessionManager: SharedPreferences? = null
    private lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var tvClassName: TextView? = null
    private var tvCourseCode:TextView? = null
    private var tvSks:TextView? = null
    private var tvSchedule:TextView? = null
    private var fabCreateMeeting: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom_detail)
        progressDialog = CustomProgressDialog(this)

        rv = findViewById<View>(R.id.rv_meetings) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(this)
        tvClassName = findViewById(R.id.tv_classroomName)
        tvCourseCode = findViewById(R.id.tv_code)
        tvSks = findViewById(R.id.tv_sks)
        tvSchedule = findViewById(R.id.tv_schedule)

        fabCreateMeeting = findViewById(R.id.fab_createMeeting)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user[sessionManager!!.TOKEN]

        try {
            val classroomDetail = JSONObject(intent.getStringExtra("lecturerclassrooms"))
            id = classroomDetail.getInt("id")
            val classroomId = classroomDetail.getInt("classroom_id")
            val className = (classroomDetail.getString("course_name").capitalizeFirstLetter() + " "
                    + classroomDetail.getString("classroom_code"))
            tvClassName!!.text = className
            val courseCode = classroomDetail.getString("course_code").toUpperCase()
            tvCourseCode!!.text = courseCode
            val sks = classroomDetail.getString("sks")
            tvSks!!.text = "$sks SKS"

            scheduleVM = ViewModelProvider(this, NewInstanceFactory())
                    .get(ClassroomScheduleViewModel::class.java)
            scheduleVM!!.setClassroomSchedule(token, classroomId)
            scheduleVM!!.getClassroomSchedule().observe(this,
                Observer<JSONArray?> { data ->
                    if (data != null && data.length() > 0) {
                        var schedules = ""
                        for (i in 0 until data.length()) {
                            val day = data.getJSONObject(i).getString("scheduled_day")
                            val startTime = data.getJSONObject(i).getString("start_time")
                            val finishTime = data.getJSONObject(i).getString("finish_time")

                            val outputTime = SimpleDateFormat("HH:mm", Locale.US)
                            val inputTime = SimpleDateFormat("HH:mm:ss", Locale.US)
                            val startTimeFormat = inputTime.parse(startTime)
                            val finishTimeFormat = inputTime.parse(finishTime)

                           schedules = if (i == 0) {
                               schedules + (day + " | " + outputTime.format(startTimeFormat) + " - "
                                        + outputTime.format(finishTimeFormat))
                           } else {
                               "$schedules\n" + (day + " | " + outputTime.format(startTimeFormat) + " - "
                                       + outputTime.format(finishTimeFormat))
                           }
                        }
                        tvSchedule!!.text = schedules
                    }
                })

            val meetingAdapter = MeetingAdapter()
            meetingAdapter.MeetingAdapter(object: MeetingAdapter.OnItemClickListener {
                @Throws(JSONException::class)
                override fun onItemClick(item: JSONObject) {
                    val intent = Intent(this@ClassroomDetailActivity, MeetingDetailActivity::class.java)
                    intent.putExtra("id", item.getInt("id"))
                    intent.putExtra("classroomId", classroomId)
                    intent.putExtra("meetingNumber", item.getString("number_of_meeting"))
                    intent.putExtra("date", item.getString("date"))
                    intent.putExtra("startTime", item.getString("start_time"))
                    intent.putExtra("finishTime", item.getString("finish_time"))
                    intent.putExtra("topic", item.getString("topic"))
                    intent.putExtra("lecturerClassroomId", id!!)
                    intent.putExtra("className", className)
                    intent.putExtra("sks", sks)
                    startActivity(intent)
                }
            })
            meetingAdapter.notifyDataSetChanged()
            rv!!.adapter = meetingAdapter

            meetingVM = ViewModelProvider(this, NewInstanceFactory()).get(
                ClassroomDetailViewModel::class.java
            )
            progressDialog.showLoading()
            meetingVM!!.setMeetings(token, classroomId)
            meetingVM!!.getMeetings()?.observe(this,
                Observer<JSONArray?> { data ->
                    if (data != null) {
                        meetingAdapter.setData(data)
                        progressDialog.hideLoading()
                    }
                })

            fabCreateMeeting?.setOnClickListener { v ->
                when (v.id) {
                    R.id.fab_createMeeting -> {
                        val i = Intent(this@ClassroomDetailActivity, AddMeetingActivity::class.java)
                        i.putExtra("lecturerclassroom", id!!)
                        i.putExtra("classroomId", classroomId)
                        i.putExtra("className", className)
                        i.putExtra("sks", sks)
                        startActivity(i)
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun String.capitalizeFirstLetter() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()

}
