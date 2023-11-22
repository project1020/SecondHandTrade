package com.secondhand.trade

import android.app.ActivityOptions
import android.content.Intent
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

    private val postID by lazy { intent.getStringExtra("postID") }
    private var postTitle: String? = null
    private var postImage: String? = null
    private var postContent: String? = null
    private var postPrice: Int? = null
    private var postSoldOut: Boolean? = false


    private val sellerUID by lazy { intent.getStringExtra("userID") }
    private var sellerProfileImage: String? = null
    private var sellerNickName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initPost()
        onWidgetClickListener()
    }

    // 게시글 내용 표시 함수
    private fun initPost() {
        val currentUID = Firebase.auth.currentUser?.uid

        // Firestore에서 게시글 내용 불러오기
        postID?.let { postID ->
            firestore.collection("board_test").document(postID).get().addOnSuccessListener { document ->
                postImage = document.getString("image")
                postTitle = document.getString("title")
                Glide.with(this).load(postImage).placeholder(whitePlaceHolderForGlide(this, 10, 10)).into(binding.imgPost)
                binding.txtTitle.text = postTitle
                binding.txtDate.text = getTimeAgo(document.getTimestamp("date")?.toDate())
                binding.txtContent.text = document.getString("content")
                binding.txtPrice.text = getString(R.string.str_post_price_won, document.getLong("price")?.let { formatNumber(it.toInt()) })
                binding.txtIsSoldOut.text = if (document.getBoolean("isSoldOut") == true) getString(R.string.str_post_sold_out) else getString(R.string.str_post_for_sale)
                binding.btnChat.isEnabled = document.getBoolean("isSoldOut") == false
            }
        }

        // Firestore에서 유저 정보 불러오기
        sellerUID?.let {
            firestore.collection("users").document(it).get().addOnSuccessListener { document ->
                sellerProfileImage = document.getString("profileImage")
                sellerNickName = document.getString("nickname")

                Glide.with(this).load(sellerProfileImage).into(binding.imgProfile)
                binding.txtNickname.text = sellerNickName
            }
        }

        // 로그인 되어있는 계정과 게시글 업로드한 계정 비교
        if (sellerUID == currentUID) {
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnChat.visibility = View.INVISIBLE
        } else {
            binding.btnEdit.visibility = View.INVISIBLE
            binding.btnChat.visibility = View.VISIBLE
        }
    }

    // 위젯 클릭 리스너 함수
    private fun onWidgetClickListener() {
        // 게시글 수정 버튼
        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this, ActivityPostEdit::class.java).apply {
                putExtra("title", postTitle)
                putExtra("image", postImage)
                putExtra("price", postPrice)
                putExtra("isSoldOut", postSoldOut)
                putExtra("userID", sellerUID)
                putExtra("content", postContent)
                //putExtra("date", getTimeAgo(item.date?.toDate()))

            })

        }

        // 쪽지 보내기 버튼
        binding.btnChat.setOnClickListener {
            startActivity(Intent(this, ActivityChatSend::class.java).apply {
                putExtra("sellerUID", sellerUID)
                putExtra("sellerProfileImage", sellerProfileImage)
                putExtra("sellerNickName", sellerNickName)
                putExtra("postTitle", postTitle)
            })
        }

        // 이미지
        binding.imgPost.setOnClickListener {
            val options  = ActivityOptions.makeSceneTransitionAnimation(this, binding.imgPost, "transitionPostImage").toBundle()
            startActivity(Intent(this, ActivityPostImage::class.java).apply {
                putExtra("postImage", postImage)
            }, options)
        }
    }
}