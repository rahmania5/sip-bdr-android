package com.rahmania.sip_bdr

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Intent
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
import kotlin.collections.indexOf as indexOf


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditMeetingActivity : AppCompatActivity() {
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
    private var btnEditMeeting: Button? = null

    private var dateCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_meeting)
        progressDialog = CustomProgressDialog(this)

        meetingNumberSpinner = findViewById(R.id.meeting_number_spinner)
        etDate = findViewById(R.id.et_date)
        etStartTime = findViewById(R.id.et_start_time)
        etFinishTime = findViewById(R.id.et_finish_time)

        btnEditMeeting = findViewById(R.id.btn_edit_meeting)

        sessionManager = SessionManager.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

        id = intent.extras?.getInt("id")
        meetingNumber = intent.extras?.getString("meetingNumber")
        date = intent.extras?.getString("date")
        startTime = intent.extras?.getString("startTime")
        finishTime = intent.extras?.getString("finishTime")

        setAutoFill()

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

        btnEditMeeting?.setOnClickListener { v ->
            when (v.id) {
                R.id.btn_edit_meeting -> {
                    editMeeting(token!!, id!!, id!!)
                }
            }
        }
    }

    private fun setAutoFill() {
        val meetingNumberArray: Array<String> = resources.getStringArray(R.array.meeting_number)
        val itemPosition = meetingNumberArray.indexOf(meetingNumber!!)
        meetingNumberSpinner?.setSelection(itemPosition)

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

        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val meetingCall: Call<ResponseBody?>? =
            apiInterface.editMeeting(token, id,
                meetingId, meetingNumber!!, date, startTime, finishTime)
        meetingCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Toast.makeText(this@EditMeetingActivity, "Pertemuan ke-" +
                        meetingNumberSpinner?.selectedItem + " berhasil diupdate!", Toast.LENGTH_SHORT).show()
                val i = Intent(this@EditMeetingActivity, MeetingDetailActivity::class.java)
                i.putExtra("id", id)
                i.putExtra("meetingNumber", meetingNumber)
                i.putExtra("date", date)
                i.putExtra("startTime", startTime)
                i.putExtra("finishTime", finishTime)
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
