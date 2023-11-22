package com.secondhand.trade

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.secondhand.trade.FunComp.Companion.clearErrorOnTextChangedAndFocus
import com.secondhand.trade.databinding.ActivityRegisterBinding
import com.secondhand.trade.databinding.BottomsheetRegisterBinding
import java.util.Calendar

class ActivityRegister : AppCompatActivity() {
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }

    private val firebaseDB by lazy { FirebaseFirestore.getInstance() }
    private val firebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val firebaseAuth by lazy { Firebase.auth }

    private val editNickname by lazy { binding.editNickname }
    private val editBirth by lazy { binding.editBirth }
    private val editEmail by lazy { binding.editEmail }
    private val editPassword by lazy { binding.editPassword }
    private val editPasswordConfirm by lazy { binding.editPasswordConfirm }
    private val inputNickname by lazy { binding.inputNickname }
    private val inputBirth by lazy { binding.inputBirth }
    private val inputEmail by lazy { binding.inputEmail }
    private val inputPassword by lazy { binding.inputPassword }
    private val inputPasswordConfirm by lazy { binding.inputPasswordConfirm }
    private val btnRegister by lazy { binding.btnRegister }

    private lateinit var profileImageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setProfileImage()
        editTextOnTextChangedAndFocus()
        onWidgetClickListener()
    }

    // EditText 유효성 검사 함수
    private fun isValidInput(nickname: String, birth: String, email: String, password: String, passwordConfirm: String): Boolean {
        inputNickname.error = when {
            nickname.trim().isEmpty() -> getString(R.string.str_register_input_nickname)
            Regex("\\s").containsMatchIn(nickname) -> getString(R.string.str_register_check_space)
            !nickname.matches(Regex("^[a-zA-Z0-9가-힣]+\$")) -> getString(R.string.str_register_special_characters)
            else -> null
        }
        inputBirth.error = if (birth.trim().isEmpty()) getString(R.string.str_register_input_birth) else null
        inputEmail.error = if (email.trim().isEmpty()) getString(R.string.str_register_input_email) else null
        inputPassword.error = if (password.trim().isEmpty()) getString(R.string.str_register_input_password) else null
        inputPasswordConfirm.error = when {
            passwordConfirm.trim().isEmpty() || passwordConfirm != password -> getString(R.string.str_register_password_different)
            else -> null
        }

        return inputNickname.error == null && inputBirth.error == null && inputEmail.error == null && inputPassword.error == null && inputPasswordConfirm.error == null
    }

    // EditText 포커스 및 입력 감지 함수
    private fun editTextOnTextChangedAndFocus() {
        editNickname.clearErrorOnTextChangedAndFocus(inputNickname)
        editBirth.clearErrorOnTextChangedAndFocus(inputBirth)
        editEmail.clearErrorOnTextChangedAndFocus(inputEmail)
        editPassword.clearErrorOnTextChangedAndFocus(inputPassword)
        editPasswordConfirm.clearErrorOnTextChangedAndFocus(inputPasswordConfirm)
    }

    // 위젯 클릭 리스너 함수
    private fun onWidgetClickListener() {
        // 생년월일 EditText 클릭 이벤트
        editBirth.setOnClickListener {
            inputBirth.error = null

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // 날짜 선택 Dialog
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val formattedDay = String.format("%02d", selectedDay)
                val date = "$selectedYear.$formattedMonth.$formattedDay"
                editBirth.setText(date)
            }, year, month, day)

            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        // 회원가입 버튼 클릭 이벤트
        btnRegister.setOnClickListener {
            val nickname = editNickname.text.toString()
            val birth = editBirth.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val passwordConfirm = editPasswordConfirm.text.toString()

            if (isValidInput(nickname, birth, email, password, passwordConfirm)) {
                doRegister(nickname, birth, email, password)
            }
        }
    }

    // 프로필 이미지 설정 함수
    private fun setProfileImage() {
        val bottomSheetBinding = BottomsheetRegisterBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        val adapterRegister = AdapterRegister(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.recyclerRegister.apply {
            layoutManager = LinearLayoutManager(this@ActivityRegister, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(ItemDecoratorDividerPadding(20))
            setHasFixedSize(true)
            adapter = adapterRegister
        }

        getProfileFromStorage { imageList ->
            // 가져온 이미지 목록을 adapter에 추가
            adapterRegister.itemList = imageList.map { DataRegister(it.toString()) }.toMutableList()
            adapterRegister.notifyDataSetChanged()

            // 이미지 목록 중 랜덤으로 하나 선택해서 프로필 이미지로 설정
            if (imageList.isNotEmpty()) {
                profileImageUrl = imageList.random().toString()
                Glide.with(this).load(profileImageUrl).into(binding.imgProfile)
            }
        }

        // 프로필 이미지 클릭
        binding.imgProfile.setOnClickListener {
            bottomSheetDialog.show()
        }

        // recyclerview 아이템 클릭
//        adapterRegister.setOnItemClickListener(object : AdapterRegister.OnItemClickListener {
//            override fun onClick(v: View, position: Int) {
//                // 프로필 이미지 변경
//                Glide.with(this@ActivityRegister).load(adapterRegister.itemList[position].image).into(binding.imgProfile)
//                profileImageUri = adapterRegister.itemList[position].image
//                bottomSheetDialog.dismiss()
//            }
//        })
        adapterRegister.setOnItemClickListener { item, _ ->
            Glide.with(this@ActivityRegister).load(item.image).into(binding.imgProfile)
            profileImageUrl = item.image
            bottomSheetDialog.dismiss()
        }
    }

    // Firebase storage에서 프로필 이미지 목록 불러오는 함수
    private fun getProfileFromStorage(callback: (List<Uri>) -> Unit) {
        val imageList = mutableListOf<Uri>()

        firebaseStorage.reference.child("image_profile").listAll()
            .addOnSuccessListener { images ->
                for (file in images.items) {
                    file.downloadUrl.addOnSuccessListener { uri ->
                        imageList.add(uri)

                        if (imageList.size == images.items.size)
                            callback(imageList)
                    }
                }
            }.addOnFailureListener {
                callback(emptyList())
            }
    }

    // 회원가입 시도 함수
    private fun doRegister(nickname: String, birth: String, email: String, password: String) {
        isNicknameAvailable(nickname) { isAvailable ->
            if (isAvailable) { // 닉네임이 존재하지 않을 때
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        registerUsers(firebaseAuth.currentUser?.uid ?: "", birth,  nickname, profileImageUrl, { // 회원가입 성공
                            Toast.makeText(this, getString(R.string.str_register_register_success), Toast.LENGTH_SHORT).show()
                            Preferences.isAutoLogin = true
                            setResult(Activity.RESULT_OK, Intent()).also { finish() }
                        }, { // 회원가입 실패
                            Toast.makeText(this, getString(R.string.str_register_register_fail), Toast.LENGTH_SHORT).show()
                        })
                    }
                    .addOnFailureListener { // 회원가입 실패
                        when {
                            // 계정이 이미 존재할 때
                            it is FirebaseAuthUserCollisionException -> {
                                inputEmail.error = getString(R.string.str_register_exist_account)
                            }
                            // 이메일 형식이 올바르지 않을 때
                            it.message?.contains("The email address is badly formatted") == true -> {
                                inputEmail.error = getString(R.string.str_register_invalid_email)
                            }
                            // 비밀번호가 6자리 미만일 때
                            it.message?.contains("The given password is invalid") == true -> {
                                inputPassword.error = getString(R.string.str_register_invalid_password)
                            }
                            // 그 외
                            else -> {
                                Toast.makeText(this, getString(R.string.str_register_register_fail), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            } else { // 닉네임이 존재할 때
                inputNickname.error = getString(R.string.str_register_exist_nickname)
            }
        }
    }

    // 닉네임 중복 확인 함수
    private fun isNicknameAvailable(nickname: String, onComplete: (Boolean) -> Unit) {
        firebaseDB.collection("users").whereEqualTo("nickname", nickname).get()
            .addOnSuccessListener {
                onComplete(it.isEmpty)
            }.addOnFailureListener {
                onComplete(false)
            }
    }

    // firestore에 닉네임, 생년월일, 프로필 이미지 등록 함수
    private fun registerUsers(userId: String, birth: String, nickname: String, profileImage: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val userMap = mapOf(
            "nickname" to nickname,
            "birth" to birth,
            "profileImage" to profileImage,
            "isLoggedIn" to true
        )

        firebaseDB.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                onFailure()
            }
    }
}