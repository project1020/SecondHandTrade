package com.secondhand.trade

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.ActivityLoginBinding

class ActivityLogin : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val inputEmail by lazy { binding.inputEmail }
    private val inputPassword by lazy { binding.inputPassword }
    private val db = FirebaseFirestore.getInstance() // 파이어베이스 Firestore 데이터베이스
    // 뒤로가기 버튼 두 번 클릭 콜백
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

        // 자동 로그인 - isAutoLogin이 true이고, 로그인이 되어있으면 실행
        if (Preferences.isAutoLogin && Firebase.auth.currentUser != null) {
            startActivity(Intent(this, ActivityMain::class.java)).also { finish() }
            return
        }

        this.onBackPressedDispatcher.addCallback(this, callback) // 뒤로가기 버튼 두 번 클릭 콜백 등록

        setTextWatchers()

        // 로그인 버튼 클릭 이벤트
        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()

            // edittext 공백 체크
            if (email.trim().isEmpty()) inputEmail.error = "이메일을 입력해 주세요." else inputEmail.error = null
            if (password.trim().isEmpty()) inputPassword.error = "비밀번호를 입력해 주세요." else inputPassword.error = null
            if (email.trim().isNotEmpty() && password.trim().isNotEmpty()) doLogin(email, password)
        }

        // 회원가입 텍스트 클릭
        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, ActivityRegister::class.java)).also { finish() }
        }
    }

    // inputLayout 입력 변화 감지 함수
    private fun setTextWatchers() {
        // edittext 입력시 inputLayout error 삭제
        binding.editEmail.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputEmail.error = null })
        binding.editPassword.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputPassword.error = null })
    }

    private fun doLogin(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val firebaseUser = task.result?.user
                val uid = firebaseUser?.uid

                if (uid != null) {
                    val userRef = db.collection("users").document(uid)
                    userRef.get().addOnSuccessListener { document ->
                        if (document.exists() && document.getBoolean("isLoggedIn") == true) {
                            Toast.makeText(this, "이미 로그인 중인 기기가 있습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Preferences.isAutoLogin = binding.switchLogin.isChecked
                            userRef.set(mapOf("isLoggedIn" to true), SetOptions.merge())
                            startActivity(Intent(this, ActivityMain::class.java)).also { finish() }
                        }
                    }
                }
            } else {
                when {
                    // 이메일 혹은 비밀번호가 틀렸을 때
                    task.exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> {
                        inputEmail.error = "이메일을 확인해 주세요."
                        inputPassword.error = "비밀번호를 확인해 주세요."
                    }
                    // 이메일 형식이 올바르지 않을 때
                    task.exception?.message?.contains("The email address is badly formatted") == true -> {
                        inputEmail.error = "이메일 형식이 올바르지 않습니다."
                    }
                    // 그 외
                    else -> {
                        Toast.makeText(this, "로그인에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}