package com.rahmania.sip_bdr.activity

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.api.ApiClient
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.helper.CustomProgressDialog
import com.rahmania.sip_bdr.helper.SharedPreferences
import com.rahmania.sip_bdr.viewModel.ClassroomScheduleViewModel
import com.rahmania.sip_bdr.viewModel.MeetingNumberViewModel
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditMeetingActivity : AppCompatActivity() {
    private var meetingNumber: String? = null
    private var date: String? = null
    private var startTime: String? = null
    private var finishTime: String? = null
    private var topic: String? = null
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SharedPreferences? = null
    lateinit var progressDialog: CustomProgressDialog
    private var scheduleVM: ClassroomScheduleViewModel? = null
    private var meetingVM: MeetingNumberViewModel? = null

    var id: Int? = null
    private var tvClassName: TextView? = null
    private var tvSks: TextView? = null
    private var tvSchedule: TextView? = null
    private var meetingNumberSpinner: Spinner? = null
    private var etDate: EditText? = null
    private var etStartTime: EditText? = null
    private var etFinishTime: EditText? = null
    private var etTopic: EditText? = null
    private var btnEditMeeting: Button? = null

    private lateinit var arrayMeetingOptions: Array<String>
    private var dateCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_meeting)
        progressDialog = CustomProgressDialog(this)

        tvClassName = findViewById(R.id.tv_classroomName)
        tvSks = findViewById(R.id.tv_sks)
        tvSchedule = findViewById(R.id.tv_schedule)
        meetingNumberSpinner = findViewById(R.id.meeting_number_spinner)
        etDate = findViewById(R.id.et_date)
        etStartTime = findViewById(R.id.et_start_time)
        etFinishTime = findViewById(R.id.et_finish_time)
        etTopic = findViewById(R.id.et_topic)

        btnEditMeeting = findViewById(R.id.btn_edit_meeting)
        arrayMeetingOptions = resources.getStringArray(R.array.meeting_number)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

        id = intent.extras?.getInt("id")
        val lecturerClassroomId = intent.extras?.getInt("lecturerClassroomId")
        val classroomId = intent.extras?.getInt("classroomId")
        tvClassName!!.text = intent.extras?.getString("className")
        tvSks!!.text = intent.extras?.getString("sks")

        scheduleVM = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(ClassroomScheduleViewModel::class.java)
        scheduleVM!!.setClassroomSchedule(token, classroomId!!)
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

        meetingNumber = intent.extras?.getString("meetingNumber")
        date = intent.extras?.getString("date")
        startTime = intent.extras?.getString("startTime")
        finishTime = intent.extras?.getString("finishTime")
        topic = intent.extras?.getString("topic")

        meetingVM = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            MeetingNumberViewModel::class.java
        )
        progressDialog.showLoading()
        meetingVM!!.setMeetingNumber(token, lecturerClassroomId)
        meetingVM!!.getMeetingNumber()?.observe(this,
            Observer<JSONArray?> { data ->
                var meeting: String
                val listMeetings = arrayListOf<String>()
                if (data != null && data.length() > 0) {
                    for (i in 0 until data.length()) {
                        meeting = data.getJSONObject(i).getString("number_of_meeting")
                        listMeetings += meeting
                    }
                    Log.d("Array Meetings", listMeetings.toString())

                    var available: String
                    val listMeetingsAvailable = arrayListOf<String>()
                    // Print all elements of first array
                    // that are not present in second array
                    for (i in arrayMeetingOptions.indices) {
                        if (!listMeetings.contains(arrayMeetingOptions[i])
                            || arrayMeetingOptions[i] == meetingNumber
                        ) {
                            available = arrayMeetingOptions[i]
                            listMeetingsAvailable += available
                        }
                    }
                    Log.d("Meetings Available", listMeetingsAvailable.toString())

                    ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        listMeetingsAvailable
                    ).also { adapter ->
                        // Apply the adapter to the spinner
                        meetingNumberSpinner?.adapter = adapter
                        adapter.notifyDataSetChanged()
                        progressDialog.hideLoading()
                    }
                    val itemPosition = listMeetingsAvailable.indexOf(meetingNumber!!)
                    meetingNumberSpinner?.setSelection(itemPosition)
                }
            })

        setAutoFill()
        setUpDialog()

        btnEditMeeting?.setOnClickListener { v ->
            when (v.id) {
                R.id.btn_edit_meeting -> {
                    editMeeting(token!!, id!!, id!!)
                }
            }
        }
    }

    private fun setUpDialog() {
        val dateDialog =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                dateCalendar.set(Calendar.YEAR, year)
                dateCalendar.set(Calendar.MONTH, monthOfYear)
                dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateLabel()
            }

        val startTimeDialog =
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                dateCalendar.set(Calendar.HOUR_OF_DAY, hour)
                dateCalendar.set(Calendar.MINUTE, minute)
                updateStartTimeLabel()
            }

        val finishTimeDialog =
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                dateCalendar.set(Calendar.HOUR_OF_DAY, hour)
                dateCalendar.set(Calendar.MINUTE, minute)
                updateFinishTimeLabel()
            }

        etDate?.setOnClickListener {
            DatePickerDialog(
                this@EditMeetingActivity, dateDialog,
                dateCalendar.get(Calendar.YEAR),
                dateCalendar.get(Calendar.MONTH),
                dateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etStartTime?.setOnClickListener {
            TimePickerDialog(
                this@EditMeetingActivity, startTimeDialog,
                dateCalendar.get(Calendar.HOUR_OF_DAY),
                dateCalendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)
            ).show()
        }

        etFinishTime?.setOnClickListener {
            TimePickerDialog(
                this@EditMeetingActivity, finishTimeDialog,
                dateCalendar.get(Calendar.HOUR_OF_DAY),
                dateCalendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)
            ).show()
        }
    }

    private fun setAutoFill() {
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateFormat = inputFormat.parse(date)
        etDate!!.setText(outputFormat.format(dateFormat))

        val outputTime = SimpleDateFormat("HH:mm", Locale.US)
        val inputTime = SimpleDateFormat("HH:mm", Locale.US)
        val startTimeFormat = inputTime.parse(startTime)
        val finishTimeFormat = inputTime.parse(finishTime)
        etStartTime!!.setText(outputTime.format(startTimeFormat))
        etFinishTime!!.setText(outputTime.format(finishTimeFormat))
        etTopic!!.setText(topic)
    }

    private fun updateDateLabel() {
        val dateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        etDate?.setText(sdf.format(dateCalendar.time))
    }

    private fun updateStartTimeLabel() {
        val timeFormat = "HH:mm"
        val sdf = SimpleDateFormat(timeFormat, Locale.US)
        etStartTime?.setText(sdf.format(dateCalendar.time))
    }

    private fun updateFinishTimeLabel() {
        val timeFormat = "HH:mm"
        val sdf = SimpleDateFormat(timeFormat, Locale.US)
        etFinishTime?.setText(sdf.format(dateCalendar.time))
    }

    private fun changeDateFormat() {
        val dateFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        etDate?.setText(sdf.format(dateCalendar.time))
    }

    private fun editMeeting(token: String, id: Int, meetingId: Int) {
        meetingNumber = meetingNumberSpinner?.selectedItem.toString()
        changeDateFormat()
        date = etDate?.text.toString()
        startTime = etStartTime?.text.toString()
        finishTime = etFinishTime?.text.toString()
        topic = etTopic?.text.toString()

        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val meetingCall: Call<ResponseBody?>? =
            apiInterface.editMeeting(token, id,
                meetingId, meetingNumber!!, date, startTime, finishTime, topic)
        meetingCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Toast.makeText(this@EditMeetingActivity, "Pertemuan ke-" +
                        meetingNumberSpinner?.selectedItem + " berhasil diupdate!", Toast.LENGTH_SHORT).show()
                val i = Intent(this@EditMeetingActivity, MainActivity::class.java)
                startActivity(i)
                finish()
                progressDialog.hideLoading()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
                Toast.makeText(this@EditMeetingActivity,
                    "Gagal menyimpan data. Mohon lengkapi field yang belum diisi.",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}
