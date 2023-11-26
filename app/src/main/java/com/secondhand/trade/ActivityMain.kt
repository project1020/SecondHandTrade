package com.secondhand.trade

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.secondhand.trade.FunComp.Companion.logout
import com.secondhand.trade.databinding.ActivityMainBinding

class HomeFilterViewModel : ViewModel() {
    // 판매 중
    private val _forSale = MutableLiveData<Boolean>(true)
    val forSale: LiveData<Boolean> get() = _forSale
    // 판매 완료
    private val _soldOut = MutableLiveData<Boolean>(true)
    val soldOut: LiveData<Boolean> get() = _soldOut
    // setter 함수
    fun setForSale(value: Boolean) { _forSale.value = value }
    fun setSoldOut(value: Boolean) { _soldOut.value = value }
}

class ActivityMain : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val layoutParent by lazy { binding.layoutParent }
    private val layoutChild by lazy { binding.layoutChild }
    private val bottombarMain by lazy { binding.bottombarMain }

    private var backPressedTime: Long = 0
    // 뒤로가기 버튼 두 번 클릭 콜백
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Snackbar.make(layoutParent, getString(R.string.str_main_press_back), 2000).show()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, callback) // 뒤로가기 버튼 콜백 등록

        changeFragment(FragmentHome())
        bottombarMain.onItemSelected = ::onNavigationItemSelected
    }

    override fun onDestroy() {
        super.onDestroy()
        // 자동 로그인 상태가 아닐 시 종료하면 로그아웃
        if (!Preferences.isAutoLogin) {
            logout(this)
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
        supportFragmentManager.beginTransaction().replace(layoutChild.id, fragment).commit()
    }
}