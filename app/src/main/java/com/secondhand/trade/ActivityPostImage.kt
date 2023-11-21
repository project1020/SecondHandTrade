package com.secondhand.trade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.secondhand.trade.databinding.ActivityPostImageBinding

class ActivityPostImage : AppCompatActivity() {
    private val binding by lazy { ActivityPostImageBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Glide.with(this).load(intent.getStringExtra("postImage")).placeholder(FunComp.whitePlaceHolderForGlide(this, 1920, 1080)).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(binding.imgPost)

        binding.btnCLose.setOnClickListener {
            finish()
        }
    }
}