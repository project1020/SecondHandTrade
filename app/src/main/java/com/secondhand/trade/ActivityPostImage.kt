package com.secondhand.trade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.secondhand.trade.FunComp.Companion.transitionWithGlide
import com.secondhand.trade.databinding.ActivityPostImageBinding

class ActivityPostImage : AppCompatActivity() {
    private val binding by lazy { ActivityPostImageBinding.inflate(layoutInflater) }
    private val postImage by lazy { intent.getStringExtra("postImage") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportPostponeEnterTransition()
        binding.imgPost.transitionWithGlide(this, postImage) {
            supportStartPostponedEnterTransition()
        }

        binding.btnClose.setOnClickListener {
            onBackPressed()
        }
    }
}