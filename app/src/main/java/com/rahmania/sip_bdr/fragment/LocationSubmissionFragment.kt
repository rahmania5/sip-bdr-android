package com.rahmania.sip_bdr.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.activity.LocationDetailActivity
import com.rahmania.sip_bdr.adapter.LocationSubmissionAdapter
import com.rahmania.sip_bdr.helper.SharedPreferences
import com.rahmania.sip_bdr.helper.SharedPreferences.SessionManager
import com.rahmania.sip_bdr.viewModel.AccountViewModel
import com.rahmania.sip_bdr.viewModel.LocationSubmissionViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class LocationSubmissionFragment : Fragment() {

    private var rv: RecyclerView? = null
    private var tvNoLocationSubmission: TextView? = null
    private var locationVM: LocationSubmissionViewModel? = null
    private var accountVM: AccountViewModel? = null
    private var sessionManager: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_submission, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val tvName: TextView = v.findViewById(R.id.tv_name) as TextView
        val tvNim: TextView = v.findViewById(R.id.tv_nip) as TextView
        rv = v.findViewById<View>(R.id.rv_locations) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(activity)
        tvNoLocationSubmission = v.findViewById<View>(R.id.tv_no_locationSubmission) as TextView
        val locationAdapter = LocationSubmissionAdapter()
        locationAdapter.LocationSubmissionAdapter(object: LocationSubmissionAdapter.OnItemClickListener {
            @Throws(JSONException::class)
            override fun onItemClick(item: JSONObject) {
                val intent = Intent(activity, LocationDetailActivity::class.java)
                intent.putExtra("studentlocation", item.toString())
                startActivity(intent)
            }
        })
        locationAdapter.notifyDataSetChanged()
        rv!!.adapter = locationAdapter
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
                    tvNim.text = stringStringHashMap[accountVM!!.nip]
                }
            })

        locationVM =
            ViewModelProvider(requireActivity(), NewInstanceFactory()).get(
                LocationSubmissionViewModel::class.java)
        locationVM!!.setStudentSubmission(token)
        locationVM!!.getStudentSubmission()?.observe(viewLifecycleOwner,
            Observer<JSONArray?> { data ->
                tvNoLocationSubmission!!.visibility = View.VISIBLE
                if (data != null && data.length() > 0) {
                    locationAdapter.setData(data)
                    tvNoLocationSubmission!!.visibility = View.GONE
                }
            })
    }
}