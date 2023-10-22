package com.secondhand.trade

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        val user = Firebase.auth.currentUser
        if (user != null) {
            val userId = user.uid
            getNickname(userId) { nickname ->
                binding.txtNickname.text = nickname ?: ""
            }

            val userEmail = user.email
            binding.txtEmail.text = userEmail ?: ""
        }

        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(mainActivity, ActivityLogin::class.java)).also { mainActivity.finish() }
        }

        return binding.root
    }

    private fun getNickname(userId: String, onComplete: (String?) -> Unit) {
        db.collection("nicknames")
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result?.documents?.firstOrNull()
                    val nickname = document?.id
                    onComplete(nickname)
                } else {
                    onComplete(null)
                }
            }
    }
}