package com.secondhand.trade

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.secondhand.trade.databinding.ActivityRegisterBinding

class ActivityRegister : AppCompatActivity() {
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    private val inputNickname by lazy { binding.inputNickname }
    private val inputEmail by lazy { binding.inputEmail }
    private val inputPassword by lazy { binding.inputPassword }
    private val inputPasswordConfirm by lazy { binding.inputPasswordConfirm }
    private lateinit var profileImageUri: String
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

        setTextWatchers()
        setProfileImage()

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

    // inputLayout 입력 변화 감지 함수
    private fun setTextWatchers() {
        // edittext 입력시 inputLayout error 삭제
        binding.editNickname.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputNickname.error = null })
        binding.editEmail.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputEmail.error = null })
        binding.editPassword.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputPassword.error = null })
        binding.editPasswordConfirm.addTextChangedListener(onTextChanged = { _, _, _, _, -> inputPasswordConfirm.error = null })
    }

    // 프로필 이미지 설정 함수
    private fun setProfileImage() {
        val parent: ViewGroup? = null
        val bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_register, parent, false)
        val bottomSheetDialog = BottomSheetDialog(this)
        val adapterRegister = AdapterRegister(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<RecyclerView>(R.id.recyclerRegister).apply {
            layoutManager = LinearLayoutManager(this@ActivityRegister, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(RecyclerViewItemDecorator(20))
            setHasFixedSize(true)
            adapter = adapterRegister
        }
        
        getProfileFromStorage { imageList ->
            // 가져온 이미지 목록을 adapter에 추가
            adapterRegister.itemList = imageList.map { DataRegister(it.toString()) }.toMutableList()
            adapterRegister.notifyDataSetChanged()

            // 이미지 목록 중 랜덤으로 하나 선택해서 프로필 이미지로 설정
            if (imageList.isNotEmpty()) {
                profileImageUri = imageList.random().toString()
                Glide.with(this).load(profileImageUri).into(binding.imgProfile)
            }
        }

        // 프로필 이미지 클릭
        binding.imgProfile.setOnClickListener {
            bottomSheetDialog.show()
        }

        // recyclerview 아이템 클릭
        adapterRegister.setOnItemClickListener(object : AdapterRegister.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                // 프로필 이미지 변경
                Glide.with(this@ActivityRegister).load(adapterRegister.itemList[position].image).into(binding.imgProfile)
                profileImageUri = adapterRegister.itemList[position].image
                bottomSheetDialog.dismiss()
            }
        })
    }

    // Firebase storage에서 프로필 이미지 목록 불러오는 함수
    private fun getProfileFromStorage(callback: (List<Uri>) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_image")
        val imageList = mutableListOf<Uri>()

        storageRef.listAll().addOnSuccessListener { listResult ->
            for (file in listResult.items) {
                file.downloadUrl.addOnSuccessListener { uri ->
                    imageList.add(uri)

                    if (imageList.size == listResult.items.size) {
                        callback(imageList)
                    }
                }
            }
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    // 회원가입 시도 함수
    private fun doRegister(nickname: String, email: String, password: String) {
        // isNickNameUnique 함수 호출
        isNicknameAvailable(nickname) { isAvailable ->
            if (isAvailable) { // 닉네임이 존재하지 않을 때
                Firebase.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) { // 회원가입 성공
                        registerUsers(Firebase.auth.currentUser?.uid ?: "", nickname, profileImageUri, { // setNickname 함수 호출
                            FunComp.logout(this,
                                onLogoutSuccess = {
                                    Toast.makeText(this, "회원가입에 성공하였습니다!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, ActivityLogin::class.java)).also { finish() }
                                }
                            )
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
            } else { // 닉네임이 존재할 때
                inputNickname.error = "이미 사용 중인 닉네임입니다."
            }
        }
    }

    // 닉네임 중복 확인 함수
    private fun isNicknameAvailable(nickname: String, onComplete: (Boolean) -> Unit) {
        db.collection("users").whereEqualTo("nickname", nickname).get().addOnCompleteListener { task ->
            if (task.isSuccessful) onComplete(task.result?.isEmpty ?: true) else onComplete(false)
        }
    }

    // firestore에 닉네임 및 프로필 이미지 등록 함수
    private fun registerUsers(userId: String, nickname: String, profileImage: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        db.collection("users").document(userId).set(mapOf("nickname" to nickname, "profileImage" to profileImage)).addOnCompleteListener {task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }
}