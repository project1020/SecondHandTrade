package com.secondhand.trade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.secondhand.trade.databinding.ActivityChatReceiveBinding

class ActivityChatReceive : AppCompatActivity() {
    private val binding by lazy { ActivityChatReceiveBinding.inflate(layoutInflater) }

    private val imgProfile by lazy { binding.imgProfile }
    private val txtTitle by lazy { binding.txtTitle }
    private val txtMessage by lazy { binding.txtMessage }
    private val txtDate by lazy { binding.txtDate }
    private val txtNickname by lazy { binding.txtNickname }

    private val profileImage by lazy { intent.getStringExtra("profileImage") }
    private val chatTitle by lazy { intent.getStringExtra("chatTitle") }
    private val chatMessage by lazy { intent.getStringExtra("chatMessage") }
    private val chatDate by lazy { intent.getStringExtra("chatDate") }
    private val senderNickname by lazy { intent.getStringExtra("senderNickname") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Glide.with(this).load(profileImage).into(imgProfile)
        txtTitle.text = chatTitle
        txtMessage.text = chatMessage
        txtDate.text = chatDate
        txtNickname.text = senderNickname
    }
}