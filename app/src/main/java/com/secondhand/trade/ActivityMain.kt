package com.secondhand.trade

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.ActivityMainBinding

class ActivityMain : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var backPressedTime: Long = 0
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Snackbar.make(binding.layoutMain, "뒤로 가기 버튼을 한 번 더 누르면 종료됩니다.", 2000).show()
            } else {
                finish()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, callback)

        changeFragment(FragmentHome())
        binding.bottombarMain.onItemSelected = ::onNavigationItemSelected
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!Preferences.isAutoLogin)
            Firebase.auth.signOut()
    }

    private fun onNavigationItemSelected(position: Int) {
        changeFragment(
            when (position) {
                0 -> FragmentHome()
                1 -> FragmentChat()
                else -> FragmentMyInfo()
            }
        )
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(binding.frameMain.id, fragment).commit()
    }
}