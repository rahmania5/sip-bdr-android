package com.rahmania.sip_bdr

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rahmania.sip_bdr.api.ApiClient
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.api.SessionManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddMeetingActivity : AppCompatActivity() {
    private var meetingNumber: String? = null
    private var date: String? = null
    private var startTime: String? = null
    private var finishTime: String? = null
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SessionManager? = null
    lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var meetingNumberSpinner: Spinner? = null
    private var etDate: EditText? = null
    private var etStartTime: EditText? = null
    private var etFinishTime: EditText? = null
    private var btnCreateMeeting: Button? = null

    private var dateCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meeting)
        progressDialog = CustomProgressDialog(this)

        meetingNumberSpinner = findViewById(R.id.meeting_number_spinner)
        etDate = findViewById(R.id.et_date)
        etStartTime = findViewById(R.id.et_start_time)
        etFinishTime = findViewById(R.id.et_finish_time)

        btnCreateMeeting = findViewById(R.id.btn_create_meeting)

        sessionManager = SessionManager.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

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

        id = intent.extras?.getInt("lecturerclassroom")

        etDate?.setOnClickListener {
            DatePickerDialog(
                this@AddMeetingActivity, dateDialog,
                dateCalendar.get(Calendar.YEAR),
                dateCalendar.get(Calendar.MONTH),
                dateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etStartTime?.setOnClickListener {
            TimePickerDialog(
                this@AddMeetingActivity, startTimeDialog,
                dateCalendar.get(Calendar.HOUR_OF_DAY),
                dateCalendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)
            ).show()
        }

        etFinishTime?.setOnClickListener {
            TimePickerDialog(
                this@AddMeetingActivity, finishTimeDialog,
                dateCalendar.get(Calendar.HOUR_OF_DAY),
                dateCalendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)
            ).show()
        }

        btnCreateMeeting?.setOnClickListener { v ->
            when (v.id) {
                R.id.btn_create_meeting -> {
                    createMeeting(token!!, id!!, id!!)
                }
            }
        }
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

    private fun createMeeting(token: String, id: Int, lecturerClassroomId: Int) {
        meetingNumber = meetingNumberSpinner?.selectedItem.toString()
        changeDateFormat()
        date = etDate?.text.toString()
        startTime = etStartTime?.text.toString()
        finishTime = etFinishTime?.text.toString()

        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val meetingCall: Call<ResponseBody?>? =
            apiInterface.createMeeting(token, id,
                meetingNumber!!, date, startTime, finishTime, lecturerClassroomId)
        meetingCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (date!!.isNotEmpty() && startTime!!.isNotEmpty() && finishTime!!.isNotEmpty()) {
                    Toast.makeText(
                        this@AddMeetingActivity,
                        "Pertemuan ke-" +
                                meetingNumberSpinner?.selectedItem + " berhasil ditambahkan!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@AddMeetingActivity,
                        "Harap lengkapi data yang belum diisi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                progressDialog.hideLoading()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
                Toast.makeText(this@AddMeetingActivity,
                    "Gagal menyimpan data. Mohon lengkapi field yang belum diisi.",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}
