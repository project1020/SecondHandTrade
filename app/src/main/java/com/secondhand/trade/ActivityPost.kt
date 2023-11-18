package com.secondhand.trade

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.FunComp.Companion.formatNumber
import com.secondhand.trade.FunComp.Companion.getTimeAgo
import com.secondhand.trade.FunComp.Companion.whitePlaceHolderForGlide
import com.secondhand.trade.databinding.ActivityPostBinding

class ActivityPost : AppCompatActivity() {
    private val binding by lazy { ActivityPostBinding.inflate(layoutInflater) }
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initPost()
        onButtonClick()
    }

    // 게시글 내용 표시 함수
    private fun initPost() {
        val postID = intent.getStringExtra("postID")
        val userID = intent.getStringExtra("userID")
        val currentUID = Firebase.auth.currentUser?.uid

        // Firestore에서 게시글 내용 불러오기
        postID?.let {
            firestore.collection("board_test").document(postID).get().addOnSuccessListener { task ->
                Glide.with(this).load(task.getString("image")).placeholder(whitePlaceHolderForGlide(this, 10, 10)).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(binding.imgPost)
                binding.txtTitle.text = task.getString("title")
                binding.txtDate.text = getTimeAgo(task.getTimestamp("date")?.toDate())
                binding.txtContent.text = task.getString("content")
                binding.txtPrice.text = "${task.getLong("price")?.let { price -> formatNumber(price.toInt()) }}원"
                binding.txtIsSoldOut.text = if (task.getBoolean("isSoldOut") == true) "거래완료" else "거래가능"
                binding.btnChat.isEnabled = task.getBoolean("isSoldOut") == false
            }
        }

        // Firestore에서 유저 정보 불러오기
        userID?.let {
            firestore.collection("users").document(it).get().addOnSuccessListener { task ->
                Glide.with(this).load(task.getString("profileImage")).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(binding.imgProfile)
                binding.txtNickname.text = task.getString("nickname")
            }
        }

        // 로그인 되어있는 계정과 게시글 업로드한 계정 비교
        if (userID == currentUID) {
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnChat.visibility = View.INVISIBLE
        } else {
            binding.btnEdit.visibility = View.INVISIBLE
            binding.btnChat.visibility = View.VISIBLE
        }
    }

    // 버튼 클릭 이벤트 함수
    private fun onButtonClick() {
        // 게시글 수정 버튼
        binding.btnEdit.setOnClickListener {
//            startActivity(Intent(this, ActivityPostEdit::class.java))
        }

        // 쪽지 보내기 버튼
        binding.btnChat.setOnClickListener {
//            startActivity(Intent(this, ActivityChat::class.java))
        }
    }
}