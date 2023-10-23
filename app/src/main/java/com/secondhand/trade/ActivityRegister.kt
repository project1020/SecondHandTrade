package com.secondhand.trade

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
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
    private val db = FirebaseFirestore.getInstance() // 파이어베이스 Firestore 데이터베이스
    // 뒤로가기 버튼 두 번 클릭 콜백
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

        this.onBackPressedDispatcher.addCallback(this, callback) // 뒤로가기 버튼 두 번 클릭 콜백 등록

        // edittext 입력시 inputLayout error 삭제
        binding.editNickname.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputNickname.error = null })
        binding.editEmail.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputEmail.error = null })
        binding.editPassword.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputPassword.error = null })
        binding.editPasswordConfirm.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputPasswordConfirm.error = null })

        // 회원가입 버튼 클릭 이벤트
        binding.btnRegister.setOnClickListener {
            val nickname = binding.editNickname.text.toString()
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            val passwordConfirm = binding.editPasswordConfirm.text.toString()

            // edittext 공백 체크
            if (nickname.trim().isEmpty()) inputNickname.error = "닉네임을 입력해 주세요." else inputNickname.error = null
            if (email.trim().isEmpty()) inputEmail.error = "이메일을 입력해 주세요." else inputEmail.error = null
            if (password.trim().isEmpty()) inputPassword.error = "비밀번호를 입력해 주세요." else inputPassword.error = null
            if (passwordConfirm.trim().isEmpty() || passwordConfirm != password) inputPasswordConfirm.error = "비밀번호가 일치하지 않습니다." else inputPasswordConfirm.error = null
            if (email.trim().isNotEmpty() && password.trim().isNotEmpty() && passwordConfirm.trim().isNotEmpty() && (password == passwordConfirm)) doRegister(nickname, email, password)
        }

        // 로그인 텍스트 클릭
        binding.txtLogin.setOnClickListener {
            startActivity(Intent(this, ActivityLogin::class.java)).also { finish() }
        }
    }

    // 회원가입 시도 함수
    private fun doRegister(nickname: String, email: String, password: String) {
        // isNickNameUnique 함수 호출
        isNicknameUnique(nickname) { isUnique ->
            if (isUnique) { // 닉네임이 존재할 때
                Firebase.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) { // 회원가입 성공
                        setNickname(Firebase.auth.currentUser?.uid ?: "", nickname, { // setNickname 함수 호출
                            Toast.makeText(this, "회원가입에 성공하였습니다!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ActivityLogin::class.java)).also { finish() }
                        }, { // 닉네임 설정 실패로 회원가입 실패
                            Toast.makeText(this, "회원가입에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        })
                    } else {
                        when {
                            // 계정이 이미 존재할 때
                            task.exception is FirebaseAuthUserCollisionException -> {
                                inputEmail.error = "이미 존재하는 계정입니다."
                            }
                            // 이메일 형식이 올바르지 않을 때
                            task.exception?.message?.contains("The email address is badly formatted") == true -> {
                                inputEmail.error = "이메일 형식이 올바르지 않습니다."
                            }
                            // 비밀번호가 6자리 미만일 때
                            task.exception?.message?.contains("The given password is invalid") == true -> {
                                inputPassword.error = "비밀번호는 6자리 이상이어야 합니다."
                            }
                            // 그 외
                            else -> {
                                Toast.makeText(this, "회원가입에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else { // 닉네임이 존재하지 않을 때
                inputNickname.error = "이미 사용 중인 닉네임입니다."
            }
        }
    }

    // 닉네임 중복 확인 함수
    private fun isNicknameUnique(nickname: String, onComplete: (Boolean) -> Unit) {
        db.collection("nicknames").document(nickname).get().addOnCompleteListener { task ->
            if (task.isSuccessful) { // 닉네임이 존재함
                val document = task.result
                onComplete(!document.exists())
            } else { // 닉네임이 존재하지 않음
                onComplete(false)
            }
        }
    }

    // 닉네임 설정 함수
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