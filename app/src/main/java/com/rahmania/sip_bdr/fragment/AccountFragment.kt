package com.rahmania.sip_bdr.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.rahmania.sip_bdr.activity.ChangePasswordActivity
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.activity.LoginActivity
import com.rahmania.sip_bdr.api.ApiClient
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.helper.SharedPreferences
import com.rahmania.sip_bdr.helper.SharedPreferences.SessionManager
import com.rahmania.sip_bdr.viewModel.AccountViewModel
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class AccountFragment : Fragment() {

    private var accountVM: AccountViewModel? = null
    private var sessionManager: SharedPreferences? = null
    private lateinit var apiInterface: ApiInterface

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val tvName: TextView = v.findViewById(R.id.tv_name) as TextView
        val tvNip: TextView = v.findViewById(R.id.tv_nip) as TextView
        val btnChangePass: Button = v.findViewById(R.id.btn_change_pass) as Button
        val btnLogout: Button = v.findViewById(R.id.btn_logout) as Button

        sessionManager = SessionManager(context)
        sessionManager!!.isLogin()
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

        accountVM =
            ViewModelProvider(
                requireActivity(),
                NewInstanceFactory()
            ).get(AccountViewModel::class.java)

        accountVM!!.setProfile(token)
        accountVM!!.getProfile().observe(
            requireActivity(),
            Observer<HashMap<String, String>> { data ->
                if (data.size > 0) {
                    tvName.text = data[accountVM!!.name]
                    tvNip.text = getString(R.string.nip, data[accountVM!!.nip])
                }
            })

        btnChangePass.setOnClickListener {
            val intent = Intent(activity, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        this.apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val user = sessionManager?.getUserDetail()
        val token = user!![sessionManager?.TOKEN]
        if (token != null) {
            Log.e("logout", token)
        }
        val logoutCall: Call<ResponseBody?>? = apiInterface.logout(token)
        logoutCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                try {
                    sessionManager?.logoutSession()
                    Toast.makeText(requireActivity(), "Berhasil logout!", Toast.LENGTH_SHORT).show()
                    val intent =
                        Intent(requireActivity(), LoginActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                Log.e("error data", t.message.toString())
            }
        })
    }

}