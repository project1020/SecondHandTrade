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
import com.secondhand.trade.FunComp.Companion.logout
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
            logout(mainActivity,
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
                Glide.with(mainActivity).load(it.first).into(binding.imgProfile)
                binding.txtNickname.text =  it.second
                binding.txtEmail.text = user.email
                binding.txtBirth.text = it.third
            }
        }
    }

    // Firestore에서 현재 로그인 되어있는 유저 정보 가져오는 함수
    private fun getNickname(userId: String, onComplete: (Triple<String?, String?, String?>) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { task ->
                val nickname = task.getString("nickname")
                val profileImage = task.getString("profileImage")
                val birth = task.getString("birth")
                onComplete(Triple(profileImage, nickname, birth))
            }.addOnFailureListener {
                onComplete(Triple(null, null, null))
            }
    }
}