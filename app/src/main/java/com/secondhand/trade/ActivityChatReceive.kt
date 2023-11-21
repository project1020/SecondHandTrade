package com.secondhand.trade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.secondhand.trade.databinding.ActivityChatReceiveBinding

class ActivityChatReceive : AppCompatActivity() {
    private val binding by lazy { ActivityChatReceiveBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // TODO : 쪽지 내용 화면에 표시하기
    }
}