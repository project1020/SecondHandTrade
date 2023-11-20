package com.secondhand.trade

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.ActivityChatBinding

class ActivityChat : AppCompatActivity(){
    private val binding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private val db by lazy {FirebaseFirestore.getInstance() }

    private val sellerUID by lazy { intent.getStringExtra("sellerUID") }
    private val sellerProfileImage by lazy { intent.getStringExtra("sellerProfileImage") }
    private val sellerNickName by lazy { intent.getStringExtra("sellerNickName") }
    private val postTitle by lazy { intent.getStringExtra("postTitle") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener{
            sendMessage()
        }

        initView()
    }

    private fun initView() {
        sellerProfileImage?.let {Glide.with(this@ActivityChat).load(it).into(binding.imgProfile) }
        sellerNickName?.let { binding.txtNickname.text = it }
        postTitle?.let { binding.txtTitle.text = it }
    }
    
    // 쪽지 보내기 함수
    private fun sendMessage() {
        val itemMap = hashMapOf(
            "sender" to Firebase.auth.currentUser?.uid,
            "message" to binding.editMessage.text.toString()
        )

        sellerUID?.let {
            db.collection("chats").document(it).collection("receivedmessage").add(itemMap).addOnSuccessListener {
                Toast.makeText(this, "판매자에게 쪽지를 보냈습니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}