package com.rahmania.sip_bdr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rahmania.sip_bdr.api.ApiClient.getClient
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.api.SessionManager.SessionManager
import com.rahmania.sip_bdr.fragment.AccountFragment
import com.rahmania.sip_bdr.fragment.ClassroomFragment
import com.rahmania.sip_bdr.fragment.LocationSubmissionFragment
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import com.rahmania.sip_bdr.api.SessionManager as ApiSessionManager


class MainActivity : AppCompatActivity() {

    private val fragmentClassroom: Fragment = ClassroomFragment()
    private val fragmentLocationSubmission: Fragment = LocationSubmissionFragment()
    private val fragmentAccount: Fragment = AccountFragment()
    private val fragmentManager: FragmentManager = supportFragmentManager
    private var active: Fragment = fragmentClassroom

    private lateinit var menu: Menu
    private lateinit var menuItem: MenuItem
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var apiInterface: ApiInterface
    private lateinit var sessionManager: ApiSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sessionManager = SessionManager(this)
        sessionManager.isLogin()

        setUpBottomNav()
    }

    private fun setUpBottomNav() {
        fragmentManager.beginTransaction().add(R.id.container, fragmentClassroom).show(fragmentClassroom).commit()
        fragmentManager.beginTransaction().add(R.id.container, fragmentLocationSubmission).hide(fragmentLocationSubmission).commit()
        fragmentManager.beginTransaction().add(R.id.container, fragmentAccount).hide(fragmentAccount).commit()

        bottomNavigationView = findViewById(R.id.nav_view)
        menu = bottomNavigationView.menu

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_classroom -> {
                    callFragment(0, fragmentClassroom)
                }
                R.id.navigation_location_submission-> {
                    callFragment(1, fragmentLocationSubmission)
                }
                R.id.navigation_account -> {
                    callFragment(2, fragmentAccount)
                }
            }
            false
        }
    }

    private fun callFragment(index: Int, fragment: Fragment) {
        menuItem = menu.getItem(index)
        menuItem.isChecked = true
        fragmentManager.beginTransaction().hide(active).show(fragment).commit()
        active = fragment
    }

    private fun logout() {
        this.apiInterface = getClient()!!.create(ApiInterface::class.java)
        val user = sessionManager.getUserDetail()
        val token = user!![sessionManager.TOKEN]
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
                    sessionManager.logoutSession()
                    Toast.makeText(this@MainActivity, "Berhasil logout!", Toast.LENGTH_SHORT).show()
                    val intent =
                        Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setMode(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun setMode(selectedMode: Int) {
        when (selectedMode) {
            R.id.item_logout -> {
                logout()
            }
        }
    }
}