package com.rahmania.sip_bdr.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.rahmania.sip_bdr.activity.ChangePasswordActivity
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.helper.SharedPreferences
import com.rahmania.sip_bdr.helper.SharedPreferences.SessionManager
import com.rahmania.sip_bdr.viewModel.AccountViewModel


class AccountFragment : Fragment() {

    private var accountVM: AccountViewModel? = null
    private var sessionManager: SharedPreferences? = null

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
            Observer<HashMap<String, String>> { stringStringHashMap ->
                if (stringStringHashMap.size > 0) {
                    tvName.text = stringStringHashMap[accountVM!!.name]
                    tvNip.text = getString(R.string.nip, stringStringHashMap[accountVM!!.nip])
                }
            })

        val btnChangePass: Button = v.findViewById(R.id.btn_change_pass) as Button

        btnChangePass.setOnClickListener {
            val intent = Intent(activity, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
    }
}