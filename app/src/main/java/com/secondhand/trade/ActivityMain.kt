package com.secondhand.trade

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.secondhand.trade.databinding.ActivityMainBinding

class ActivityMain : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    // 뒤로가기 버튼 두 번 클릭 콜백
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

        this.onBackPressedDispatcher.addCallback(this, callback) // 뒤로가기 버튼 두 번 클릭 콜백 등록

        changeFragment(FragmentHome())
        binding.bottombarMain.onItemSelected = ::onNavigationItemSelected
    }

    override fun onDestroy() {
        super.onDestroy()
        // 자동 로그인 상태가 아닐 시 종료하면 로그아웃
        if (!Preferences.isAutoLogin) {
            FunComp.logout(this)
        }
    }

    // 하단바 클릭 이벤트
    private fun onNavigationItemSelected(position: Int) {
        changeFragment(
            when (position) {
                0 -> FragmentHome()
                1 -> FragmentChat()
                else -> FragmentMyInfo()
            }
        )
    }

    // fragment 전환 함수
    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(binding.frameMain.id, fragment).commit()
    }
}