package com.secondhand.trade

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.ActivityLoginBinding

class ActivityLogin : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val inputEmail by lazy { binding.inputEmail }
    private val inputPassword by lazy { binding.inputPassword }
    private var backPressedTime: Long = 0
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Snackbar.make(binding.layoutLogin, "뒤로 가기 버튼을 한 번 더 누르면 종료됩니다.", 2000).show()
            } else {
                finish()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (Preferences.isAutoLogin && Firebase.auth.currentUser != null) {
            startActivity(Intent(this, ActivityMain::class.java)).also { finish() }
            return
        }

        this.onBackPressedDispatcher.addCallback(this, callback)

        binding.btnLogin.setOnClickListener {
            Preferences.isAutoLogin = binding.switchLogin.isChecked

            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()

            if (email.trim().isEmpty()) inputEmail.error = "이메일을 입력해 주세요." else inputEmail.error = null
            if (password.trim().isEmpty()) inputPassword.error = "비밀번호를 입력해 주세요." else inputPassword.error = null
            if (email.trim().isNotEmpty() && password.trim().isNotEmpty()) doLogin(email, password)
        }

        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, ActivityRegister::class.java)).also { finish() }
        }
    }

    private fun doLogin(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, ActivityMain::class.java)).also { finish() }
            } else {
                when {
                    task.exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> {
                        inputEmail.error = "이메일을 확인해 주세요."
                        inputPassword.error = "비밀번호를 확인해 주세요."
                    }
                    task.exception?.message?.contains("The email address is badly formatted") == true -> {
                        inputEmail.error = "이메일 형식이 올바르지 않습니다."
                    }
                    else -> {
                        Toast.makeText(this, "로그인에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}