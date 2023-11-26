package com.secondhand.trade

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.ActivityChatSendBinding
import java.util.Date

class ActivityChatSend : AppCompatActivity(){
    private val binding by lazy { ActivityChatSendBinding.inflate(layoutInflater) }

    private val firebaseDB by lazy { FirebaseFirestore.getInstance() }
    private val currentUserID by lazy { Firebase.auth.currentUser?.uid }

    private val imgProfile by lazy { binding.imgProfile }
    private val txtTitle by lazy { binding.txtTitle }
    private val editMessage by lazy { binding.editMessage }
    private val txtNickname by lazy { binding.txtNickname }
    private val btnSend by lazy { binding.btnSend }

    private val sellerProfileImage by lazy { intent.getStringExtra("sellerProfileImage") }
    private val postTitle by lazy { intent.getStringExtra("postTitle") }
    private val sellerNickName by lazy { intent.getStringExtra("sellerNickName") }
    private val sellerUID by lazy { intent.getStringExtra("sellerUID") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initWidget()
    }

    private fun initWidget() {
        Glide.with(this@ActivityChatSend).load(sellerProfileImage).into(imgProfile)
        txtTitle.text = postTitle
        txtNickname.text = sellerNickName

        btnSend.setOnClickListener{ sendMessage() }
    }

    // 쪽지 보내기 함수
    private fun sendMessage() {
        val message = editMessage.text.toString()

        if (message.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.str_chatsend_input_message), Toast.LENGTH_SHORT).show()
        } else {
            val itemMap = hashMapOf(
                "title" to postTitle,
                "message" to message,
                "date" to Date(),
                "sender" to currentUserID
            )

            sellerUID?.let {
                firebaseDB.collection("chats").document(it).collection("receivedmessage").add(itemMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.str_chatsend_send_success), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, getString(R.string.str_chatsend_send_failed), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}