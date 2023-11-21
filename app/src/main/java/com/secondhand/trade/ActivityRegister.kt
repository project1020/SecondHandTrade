package com.secondhand.trade

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.secondhand.trade.databinding.ActivityRegisterBinding
import java.util.Calendar

class ActivityRegister : AppCompatActivity() {
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    private val inputNickname by lazy { binding.inputNickname }
    private val inputBirth by lazy { binding.inputBirth }
    private val inputEmail by lazy { binding.inputEmail }
    private val inputPassword by lazy { binding.inputPassword }
    private val inputPasswordConfirm by lazy { binding.inputPasswordConfirm }
    private lateinit var profileImageUri: String
    private val db = FirebaseFirestore.getInstance() // 파이어베이스 Firestore 데이터베이스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        editTextOnFocusAndTextChanged()
        setProfileImage()

        // 생년월일 EditText 클릭 이벤트
        binding.editBirth.setOnClickListener {
            inputBirth.error = null

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val formattedDay = String.format("%02d", selectedDay)
                val date = "$selectedYear.$formattedMonth.$formattedDay"
                binding.editBirth.setText(date)
            }, year, month, day)

            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        // 회원가입 버튼 클릭 이벤트
        binding.btnRegister.setOnClickListener {
            val nickname = binding.editNickname.text.toString()
            val birth = binding.editBirth.text.toString()
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            val passwordConfirm = binding.editPasswordConfirm.text.toString()

            if (isValidInput(nickname, birth, email, password, passwordConfirm)) {
                doRegister(nickname, birth, email, password)
            }
        }
    }

    // EditText 유효성 검사 함수
    private fun isValidInput(nickname: String, birth: String, email: String, password: String, passwordConfirm: String): Boolean {
        inputNickname.error = when {
            nickname.trim().isEmpty() -> "닉네임을 입력해 주세요."
            Regex("\\s").containsMatchIn(nickname) -> "공백을 확인해 주세요."
            !nickname.matches(Regex("^[a-zA-Z0-9가-힣]+\$")) -> "특수문자는 사용이 불가능합니다."
            else -> null
        }
        inputBirth.error = if (birth.trim().isEmpty()) "생년월일을 입력해 주세요." else null
        inputEmail.error = if (email.trim().isEmpty()) "이메일을 입력해 주세요." else null
        inputPassword.error = if (password.trim().isEmpty()) "비밀번호를 입력해 주세요." else null
        inputPasswordConfirm.error = when {
            passwordConfirm.trim().isEmpty() -> "비밀번호가 일치하지 않습니다."
            passwordConfirm != password -> "비밀번호가 일치하지 않습니다."
            else -> null
        }

        return inputNickname.error == null && inputBirth.error == null && inputEmail.error == null && inputPassword.error == null && inputPasswordConfirm.error == null
    }

    // EditText 포커스 및 입력 감지 함수
    private fun editTextOnFocusAndTextChanged() {
        binding.editNickname.apply {
            addTextChangedListener(onTextChanged = { _, _, _, _, -> inputNickname.error = null })
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) inputNickname.error = null }
        }

        binding.editBirth.apply {
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) inputBirth.error = null }
        }

        binding.editEmail.apply {
            addTextChangedListener(onTextChanged = { _, _, _, _, -> inputEmail.error = null })
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) inputEmail.error = null }
        }

        binding.editPassword.apply {
            addTextChangedListener(onTextChanged = { _, _, _, _, -> inputPassword.error = null })
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) inputPassword.error = null }
        }

        binding.editPasswordConfirm.apply {
            addTextChangedListener(onTextChanged = { _, _, _, _, -> inputPasswordConfirm.error = null })
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) inputPasswordConfirm.error = null }
        }
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
        val imageList = mutableListOf<Uri>()

        FirebaseStorage.getInstance().reference.child("image_profile").listAll().addOnSuccessListener { listResult ->
            for (file in listResult.items) {
                file.downloadUrl.addOnSuccessListener { uri ->
                    imageList.add(uri)

                    if (imageList.size == listResult.items.size)
                        callback(imageList)
                }
            }
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    // 회원가입 시도 함수
    private fun doRegister(nickname: String, birth: String, email: String, password: String) {
        // isNickNameUnique 함수 호출
        isNicknameAvailable(nickname) { isAvailable ->
            if (isAvailable) { // 닉네임이 존재하지 않을 때
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { // 회원가입 성공
                        registerUsers(Firebase.auth.currentUser?.uid ?: "", birth,  nickname, profileImageUri, {
                            Toast.makeText(this, "회원가입에 성공하였습니다!", Toast.LENGTH_SHORT).show()
                            Preferences.isAutoLogin = true
                            //startActivity(Intent(this, ActivityLogin::class.java)).also { finish() }
                            setResult(Activity.RESULT_OK, Intent()).also { finish() }
                        }, { // 닉네임 설정 실패로 회원가입 실패
                            Toast.makeText(this, "회원가입에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        })
                    }
                    .addOnFailureListener { // 회원가입 실패
                        when {
                            // 계정이 이미 존재할 때
                            it is FirebaseAuthUserCollisionException -> {
                                inputEmail.error = "이미 존재하는 계정입니다."
                            }
                            // 이메일 형식이 올바르지 않을 때
                            it.message?.contains("The email address is badly formatted") == true -> {
                                inputEmail.error = "이메일 형식이 올바르지 않습니다."
                            }
                            // 비밀번호가 6자리 미만일 때
                            it.message?.contains("The given password is invalid") == true -> {
                                inputPassword.error = "비밀번호는 6자리 이상이어야 합니다."
                            }
                            // 그 외
                            else -> {
                                Toast.makeText(this, "회원가입에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
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
            if (task.isSuccessful)
                onComplete(task.result?.isEmpty ?: true)
            else
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
        db.collection("users").document(userId).set(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful)
                onSuccess()
            else
                onFailure()
        }
    }
}