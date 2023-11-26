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
    private lateinit var mainActivity: ActivityMain

    private val firebaseDB by lazy { FirebaseFirestore.getInstance() }
    private val currentUser by lazy { Firebase.auth.currentUser }

    private val imgProfile by lazy { binding.imgProfile }
    private val txtNickname by lazy { binding.txtNickname }
    private val txtEmail by lazy { binding.txtEmail }
    private val txtBirth by lazy { binding.txtBirth }
    private val btnLogout by lazy { binding.btnLogout }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as ActivityMain
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initWidget()

        return binding.root
    }

    private fun initWidget() {
        currentUser?.let { user ->
            val userId = user.uid
            getNickname(userId) {
                Glide.with(mainActivity).load(it.first).into(imgProfile)
                txtNickname.text = it.second
                txtEmail.text = user.email
                txtBirth.text = it.third
            }
        }

        btnLogout.setOnClickListener {
            logout(mainActivity,
                onLogoutSuccess = {
                    startActivity(Intent(mainActivity, ActivityLogin::class.java)).also { mainActivity.finish() }
                }
            )
        }
    }

    // Firestore에서 현재 로그인 되어있는 유저 정보 가져오는 함수
    private fun getNickname(userId: String, onComplete: (Triple<String?, String?, String?>) -> Unit) {
        firebaseDB.collection("users").document(userId).get()
            .addOnSuccessListener { task ->
                val nickname = task.getString("nickname")
                val profileImage = task.getString("profileImage")
                val birth = task.getString("birth")
                onComplete(Triple(profileImage, nickname, birth))
            }
            .addOnFailureListener {
                onComplete(Triple(null, null, null))
            }
    }
}