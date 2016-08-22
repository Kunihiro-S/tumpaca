package com.tumpaca.tumpaca.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.fragment.AuthFragment
import com.tumpaca.tumpaca.fragment.DashboardFragment
import com.tumpaca.tumpaca.fragment.SettingsFragment

/**
 * Tumpaca クラスのメインアクティビティ。
 */
class MainActivity : AppCompatActivity(),
        DashboardFragment.DashboardFragmentListener,
        SettingsFragment.SettingsFragmentListener {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate()")
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val start = AuthFragment()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.replace(R.id.fragment_container, start)
            ft.commit()
        }
    }

    override fun showSettings(fr: DashboardFragment) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val settingsFragment = SettingsFragment()
        ft.add(R.id.fragment_container, settingsFragment)
        ft.show(settingsFragment)
        ft.addToBackStack(null)
        ft.hide(fr)
        ft.commit()
    }

    override fun hideSettings() {
        onBackPressed()
    }
}
