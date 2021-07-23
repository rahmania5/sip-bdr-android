package com.rahmania.sip_bdr.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rahmania.sip_bdr.R
import com.rahmania.sip_bdr.api.ApiInterface
import com.rahmania.sip_bdr.fragment.AccountFragment
import com.rahmania.sip_bdr.fragment.ClassroomFragment
import com.rahmania.sip_bdr.fragment.LocationSubmissionFragment
import com.rahmania.sip_bdr.helper.SharedPreferences
import com.rahmania.sip_bdr.helper.SharedPreferences.SessionManager


class MainActivity : AppCompatActivity() {

    private val fragmentClassroom: Fragment = ClassroomFragment()
    private val fragmentLocationSubmission: Fragment = LocationSubmissionFragment()
    private val fragmentAccount: Fragment = AccountFragment()
    private val fragmentManager: FragmentManager = supportFragmentManager
    private var active: Fragment = fragmentClassroom

    private lateinit var menu: Menu
    private lateinit var menuItem: MenuItem
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sessionManager: SharedPreferences

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
                R.id.navigation_location_submission -> {
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
}