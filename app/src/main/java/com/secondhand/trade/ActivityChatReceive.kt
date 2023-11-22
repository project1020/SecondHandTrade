package com.secondhand.trade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.secondhand.trade.databinding.ActivityChatReceiveBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityChatReceive : AppCompatActivity() {
    private val binding by lazy { ActivityChatReceiveBinding.inflate(layoutInflater) }
    var formattedDate=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val profileImage = intent.getStringExtra("profileImage")
        val nickname = intent.getStringExtra("nickname")
        val message = intent.getStringExtra("message")
        val receivedDate = intent.getStringExtra("date")
        println(receivedDate)
//        if (receivedDate != null) {
//            val stringDate = Date(receivedDate.time)
//            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            formattedDate = sdf.format(date)
//        }


        // TODO : 쪽지 내용 화면에 표시하기
        Glide.with(this@ActivityChatReceive)
            .load(profileImage)
            .into(binding.imgProfile2)
        binding.Username.text = nickname
        binding.receiveDate.text = receivedDate
        binding.messageContext.text=message
    }
}