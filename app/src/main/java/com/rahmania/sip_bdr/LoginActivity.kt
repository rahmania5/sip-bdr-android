package com.rahmania.sip_bdr.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rahmania.sip_bdr.MainActivity
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.api.ApiClient
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.api.SessionManager.SessionManager
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import com.rahmania.sip_bdr.api.SessionManager as ApiSessionManager


class LoginActivity : AppCompatActivity() {

    var username: String? = null
    var password: String? = null
    private lateinit var apiInterface: ApiInterface
    private lateinit var sessionManager: ApiSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(context = this@LoginActivity)
        if (sessionManager.checkToken()) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        var v_username = findViewById<EditText>(R.id.username)
        var v_password = findViewById<EditText>(R.id.password)
        val v_login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        v_login.setOnClickListener { v ->
            when (v.id) {
                R.id.login -> {
                    username = v_username.text.toString()
                    password = v_password.text.toString()
                    login(username!!, password!!)
                    loading.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun login(username: String, password: String) {
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val loginCall: Call<ResponseBody?>? = apiInterface.loginResponse(username, password)
        loginCall?.enqueue {
            fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    var jsonRESULTS: JSONObject?
                    try {
                        jsonRESULTS = JSONObject(response.body()!!.string())
                        val jsonData = jsonRESULTS.getJSONObject("data")
                        Log.e("jsonData", jsonData.toString())
                        if (jsonData.length() != 0) {
                            val token = jsonData.getString("access_token")
                            val name = jsonData.getString("name")
                            val nip = jsonData.getString("nip")
                            sessionManager.createLoginSession(token, name, nip)
                            Toast.makeText(this@LoginActivity, name, Toast.LENGTH_SHORT).show()
                            val intent =
                                Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Gagal Login", Toast.LENGTH_SHORT).show()
                }
            }

            fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {
                Log.e("error data", t.message.toString())
            }
        }
    }
    private fun <T> Call<T>?.enqueue(callback: CallBackKt<T>.() -> Unit) {
        val callBackKt = CallBackKt<T>()
        callback.invoke(callBackKt)
        this?.enqueue(callBackKt)
    }
}