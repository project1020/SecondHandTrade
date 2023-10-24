package com.secondhand.trade

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.FragmentMyinfoBinding

class FragmentMyInfo : Fragment() {
    private val binding by lazy { FragmentMyinfoBinding.inflate(layoutInflater) }
    private val db = FirebaseFirestore.getInstance()

    private lateinit var mainActivity: ActivityMain
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as ActivityMain
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initProfile()

        // 로그아웃 버튼 클릭
        binding.btnLogout.setOnClickListener {
            // FunComp 클래스의 로그아웃 함수 실행
            FunComp.logout(mainActivity,
                onLogoutSuccess = {
                    startActivity(Intent(mainActivity, ActivityLogin::class.java)).also { mainActivity.finish() }
                }
            )
        }
        return binding.root
    }

    // 프로필 표시 함수
    private fun initProfile() {
        Firebase.auth.currentUser?.let { user ->
            val userId = user.uid
            getNickname(userId) {
                Glide.with(mainActivity).load(it.second).into(binding.imgProfile)
                binding.txtNickname.text =  it.first
                binding.txtEmail.text = user.email
            }
        }
    }

    // 로그인 되어있는 계정의 닉네임을 Firestore 데이터베이스에서 가져오는 함수
    private fun getNickname(userId: String, onComplete: (Pair<String?, String?>) -> Unit) {
        db.collection("nicknames").whereEqualTo("userId", userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result?.documents?.firstOrNull()
                val nickname = document?.id
                val profileImage = document?.getString("profileImage")
                onComplete(Pair(nickname, profileImage))
            } else {
                onComplete(Pair(null, null))
            }
        }
    }
}