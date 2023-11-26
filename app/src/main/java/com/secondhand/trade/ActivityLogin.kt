package com.secondhand.trade

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.FunComp.Companion.clearErrorOnTextChangedAndFocus
import com.secondhand.trade.databinding.ActivityLoginBinding

class ActivityLogin : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private val firebaseAuth by lazy { Firebase.auth }

    private val editEmail by lazy { binding.editEmail }
    private val editPassword by lazy { binding.editPassword }
    private val inputEmail by lazy { binding.inputEmail }
    private val inputPassword by lazy { binding.inputPassword }
    private val txtRegister by lazy { binding.txtRegister }
    private val btnLogin by lazy { binding.btnLogin }
    private val switchAutoLogin by lazy { binding.switchAutoLogin }


    private var backPressedTime: Long = 0
    // 뒤로가기 버튼 두 번 클릭 콜백
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime >= 2000) {
                backPressedTime = System.currentTimeMillis()
                Snackbar.make(binding.layoutParent, getString(R.string.str_login_press_back), 2000).show()
            } else {
                finish()
            }
        }
    }

    private val startForRegisterResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            autoLogin()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, callback) // 뒤로가기 버튼 콜백 등록

        autoLogin()
        initWidget()
        editTextOnTextChangedAndFocus()
    }

    // 자동 로그인 함수
    private fun autoLogin() {
        // isAutoLogin이 true이고, 로그인이 되어있으면 실행
        if (Preferences.isAutoLogin && Firebase.auth.currentUser != null) {
            startActivity(Intent(this, ActivityMain::class.java)).also { finish() }
            return
        }
    }

    // EditText 유효성 검사 함수
    private fun isValidInput(email: String, password: String): Boolean {
        inputEmail.error = if (email.trim().isEmpty()) getString(R.string.str_register_input_email) else null
        inputPassword.error = if (password.trim().isEmpty()) getString(R.string.str_register_input_password) else null

        return inputEmail.error == null && inputPassword.error == null
    }

    // EditText 포커스 및 입력 감지 함수
    private fun editTextOnTextChangedAndFocus() {
        editEmail.clearErrorOnTextChangedAndFocus(inputEmail)
        editPassword.clearErrorOnTextChangedAndFocus(inputPassword)
    }

    private fun initWidget() {
        // 로그인 버튼 클릭 이벤트
        btnLogin.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if (isValidInput(email, password)) {
                doLogin(email, password)
            }
        }

        // 회원가입 텍스트 클릭
        txtRegister.setOnClickListener {
            startForRegisterResult.launch(Intent(this, ActivityRegister::class.java))
        }
    }

    // 로그인 시도 함수
    private fun doLogin(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Preferences.isAutoLogin = switchAutoLogin.isChecked
                startActivity(Intent(this, ActivityMain::class.java)).also { finish() }
            }
            .addOnFailureListener {
                when {
                    // 이메일 혹은 비밀번호가 틀렸을 때
                    it.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> {
                        inputEmail.error = getString(R.string.str_login_check_email)
                        inputPassword.error = getString(R.string.str_login_check_password)
                    }
                    // 이메일 형식이 올바르지 않을 때
                    it.message?.contains("The email address is badly formatted") == true -> {
                        inputEmail.error = getString(R.string.str_login_invalid_email)
                    }
                    // 그 외
                    else -> {
                        Toast.makeText(this, getString(R.string.str_login_login_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}