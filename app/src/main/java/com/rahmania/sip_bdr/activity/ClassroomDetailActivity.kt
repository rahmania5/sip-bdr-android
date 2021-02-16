package com.rahmania.sip_bdr

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
import com.rahmania.sip_bdr.adapter.MeetingAdapter
import com.rahmania.sip_bdr.api.SessionManager
import com.rahmania.sip_bdr.viewModel.ClassroomDetailViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ClassroomDetailActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    private var meetingVM: ClassroomDetailViewModel? = null
    private var sessionManager: SessionManager? = null
    lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var tvClassName: TextView? = null
    private var tvCourseCode:TextView? = null
    private var tvSks:TextView? = null
    private var tvDay:TextView? = null
    private var tvTime:TextView? = null
    //private var tvLecturer:TextView? = null
    private var fabCreateMeeting: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom_detail)
        progressDialog = CustomProgressDialog(this)

        rv = findViewById<View>(R.id.rv_meetings) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(this)
        tvClassName = findViewById(R.id.tv_classroomName)
        tvCourseCode = findViewById(R.id.tv_courseCode)
        tvSks = findViewById(R.id.tv_sks)
        tvDay = findViewById(R.id.tv_day)
        tvTime = findViewById(R.id.tv_time)
        //tvLecturer = findViewById(R.id.tv_lecturer)

        fabCreateMeeting = findViewById(R.id.fab_createMeeting)

        sessionManager = SessionManager.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

        val meetingAdapter = MeetingAdapter()
        meetingAdapter.MeetingAdapter(object: MeetingAdapter.OnItemClickListener {
            @Throws(JSONException::class)
            override fun onItemClick(item: JSONObject) {
                val intent = Intent(this@ClassroomDetailActivity, MeetingDetailActivity::class.java)
                intent.putExtra("id", item.getInt("id"))
                intent.putExtra("meetingNumber", item.getString("number_of_meeting"))
                intent.putExtra("date", item.getString("date"))
                intent.putExtra("startTime", item.getString("start_time"))
                intent.putExtra("finishTime", item.getString("finish_time"))
                startActivity(intent)
            }
        })
        meetingAdapter.notifyDataSetChanged()
        rv!!.adapter = meetingAdapter

        try {
            val intent = intent
            val classroomDetail = JSONObject(intent.getStringExtra("lecturerclassrooms"))
            id = classroomDetail.getInt("id")
            val startTime = classroomDetail.getString("start_time")
            val finishTime = classroomDetail.getString("finish_time")

            val outputTime = SimpleDateFormat("HH:mm", Locale.US)
            val inputTime = SimpleDateFormat("HH:mm:ss", Locale.US)
            val startTimeFormat = inputTime.parse(startTime)
            val finishTimeFormat = inputTime.parse(finishTime)

            tvClassName!!.text = (classroomDetail.getString("course_name").capitalizeFirstLetter() + " "
                    + classroomDetail.getString("classroom_code"))
            tvCourseCode!!.text = classroomDetail.getString("course_code").toUpperCase(Locale.ROOT)

//            var lecturerName = ""
//            for (i in 0 until classroomDetail.getJSONArray("lecturers").length()) {
//                lecturerName = if (i == 0) {
//                    lecturerName + classroomDetail.getJSONArray("lecturers").getJSONObject(i)
//                        .getString("name")
//                } else {
//                    "$lecturerName, " + classroomDetail.getJSONArray("lecturers").getJSONObject(i)
//                        .getString("name")
//                }
//            }
//            tvLecturer!!.text = lecturerName
            tvSks!!.text = (classroomDetail.getString("sks") + " SKS")
            tvDay!!.text = classroomDetail.getString("scheduled_day")
            tvTime!!.text = (outputTime.format(startTimeFormat) + " - "
                    + outputTime.format(finishTimeFormat))

            meetingVM = ViewModelProvider(this, NewInstanceFactory()).get(
                ClassroomDetailViewModel::class.java
            )
            progressDialog.showLoading()
            meetingVM!!.setMeetings(token, id)
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
                        startActivity(i)
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun String.capitalizeFirstLetter() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()

}
