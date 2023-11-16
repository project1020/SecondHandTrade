package com.secondhand.trade

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.ActivitySendBinding

class SendMessageActivity : AppCompatActivity(){
    private val binding by lazy { ActivitySendBinding.inflate(layoutInflater) }
    private val db = FirebaseFirestore.getInstance()

    private lateinit var messagetext: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        messagetext = findViewById(R.id.messagetext)
        initProfile()
        binding.btnsend.setOnClickListener{
            addItem()
            finish()
        }
    //
    }

    private fun initProfile() {
        Firebase.auth.currentUser?.let { user ->
            val userId = user.uid
            getNickname(userId) { nickname, profileImage ->
                Glide.with(this@SendMessageActivity).load(profileImage).into(binding.imgProfile)
                binding.txtNickname.text = nickname

            }
        }
    }

    // 로그인 되어있는 계정의 닉네임을 Firestore 데이터베이스에서 가져오는 함수
    private fun getNickname(userId: String, onComplete: (String?, String?) -> Unit) {
        db.collection("users").document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var nickname = task.result.getString("nickname")
                val profileImage = task.result.getString("profileImage")
                onComplete(nickname+"님에게 메세지 보내기", profileImage)
            } else {
                onComplete(null, null)
            }
        }
    }
    private fun addItem() {
        val receivedMessagesCollectionRef = db.collection("chats").document(userId).collection("receivedmessage")
        Firebase.auth.currentUser?.let { user ->
            val userId = user.uid
            db.collection("users").document(userId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val name = task.result.getString("nickname")
                    val messageinput = binding.messagetext.text.toString()
                    val itemMap = hashMapOf(
                        "sender" to name,
                        "text" to messageinput
                    )
                    receivedMessagesCollectionRef.add(itemMap).addOnSuccessListener { }.addOnFailureListener {  }
                }
            }
        }
    }
}