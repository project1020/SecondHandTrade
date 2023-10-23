package com.secondhand.trade

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.ActivityRegisterBinding

class ActivityRegister : AppCompatActivity() {
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    private val inputNickname by lazy { binding.inputNickname }
    private val inputEmail by lazy { binding.inputEmail }
    private val inputPassword by lazy { binding.inputPassword }
    private val inputPasswordConfirm by lazy { binding.inputPasswordConfirm }
    private val db = FirebaseFirestore.getInstance()
    private var backPressedTime: Long = 0
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Snackbar.make(binding.layoutRegister, "뒤로 가기 버튼을 한 번 더 누르면 종료됩니다.", 2000).show()
            } else {
                finish()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, callback)

        binding.btnRegister.setOnClickListener {
            val nickname = binding.editNickname.text.toString()
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            val passwordConfirm = binding.editPasswordConfirm.text.toString()

            if (nickname.trim().isEmpty()) inputNickname.error = "닉네임을 입력해 주세요." else inputNickname.error = null
            if (email.trim().isEmpty()) inputEmail.error = "이메일을 입력해 주세요." else inputEmail.error = null
            if (password.trim().isEmpty()) inputPassword.error = "비밀번호를 입력해 주세요." else inputPassword.error = null
            if (passwordConfirm.trim().isEmpty() || passwordConfirm != password) inputPasswordConfirm.error = "비밀번호가 일치하지 않습니다." else inputPasswordConfirm.error = null
            if (email.trim().isNotEmpty() && password.trim().isNotEmpty() && passwordConfirm.trim().isNotEmpty() && (password == passwordConfirm)) doRegister(nickname, email, password)
        }

        binding.txtLogin.setOnClickListener {
            startActivity(Intent(this, ActivityLogin::class.java)).also { finish() }
        }
    }

    private fun doRegister(nickname: String, email: String, password: String) {
        isNicknameUnique(nickname) { isUnique ->
            if (isUnique) {
                Firebase.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        setNickname(Firebase.auth.currentUser?.uid ?: "", nickname, {
                            Toast.makeText(this, "회원가입에 성공하였습니다!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ActivityLogin::class.java)).also { finish() }
                        }, { })
                    } else {
                        when {
                            task.exception is FirebaseAuthUserCollisionException -> {
                                inputEmail.error = "이미 존재하는 계정입니다."
                            }
                            task.exception?.message?.contains("The email address is badly formatted") == true -> {
                                inputEmail.error = "이메일 형식이 올바르지 않습니다."
                            }
                            task.exception?.message?.contains("The given password is invalid") == true -> {
                                inputPassword.error = "비밀번호는 6자리 이상이어야 합니다."
                            }
                            else -> {
                                Toast.makeText(this, "회원가입에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                inputNickname.error = "이미 사용 중인 닉네임입니다."
            }
        }
    }

    private fun isNicknameUnique(nickname: String, onComplete: (Boolean) -> Unit) {
        db.collection("nicknames").document(nickname).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                onComplete(!document.exists())
            } else {
                onComplete(false)
            }
        }
    }

    private fun setNickname(userId: String, nickname: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        db.collection("nicknames").document(nickname).set(mapOf("userId" to userId)).addOnSuccessListener {
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(nickname)
                .build()

            FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdate)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
        }.addOnFailureListener {
            onFailure()
        }
    }
}