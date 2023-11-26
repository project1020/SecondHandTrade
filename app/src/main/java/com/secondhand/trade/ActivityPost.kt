package com.secondhand.trade

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.FunComp.Companion.formatNumber
import com.secondhand.trade.FunComp.Companion.getTimeAgo
import com.secondhand.trade.FunComp.Companion.whitePlaceHolderForGlide
import com.secondhand.trade.databinding.ActivityPostBinding

class ActivityPost : AppCompatActivity() {
    private val binding by lazy { ActivityPostBinding.inflate(layoutInflater) }

    private val firebaseDB by lazy { FirebaseFirestore.getInstance() }
    private val currentUserID by lazy { Firebase.auth.currentUser?.uid }

    private val postID by lazy { intent.getStringExtra("postID") }
    private val sellerUID by lazy { intent.getStringExtra("userID") }

    private var sellerProfileImage: String? = null
    private var sellerNickName: String? = null
    private var postImage: String? = null
    private var postTitle: String? = null
    private var postContent: String? = null
    private var postDate: String? = null
    private var postPrice: Long? = null
    private var postIsSoldOut: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initWidget()
    }

    override fun onStart() {
        super.onStart()
        initPost()
    }

    // 게시글 내용 표시 함수
    private fun initPost() {
        // Firestore에서 게시글 내용 불러오기
        postID?.let { postID ->
            firebaseDB.collection("board").document(postID).get()
                .addOnSuccessListener { document ->
                    postImage = document.getString("image")
                    postTitle = document.getString("title")
                    postContent = document.getString("content")
                    postPrice = document.getLong("price")
                    postDate = getTimeAgo(document.getTimestamp("date")?.toDate())
                    postIsSoldOut = document.getBoolean("isSoldOut")

                    Glide.with(this).load(postImage).placeholder(whitePlaceHolderForGlide(this, 10, 10)).into(binding.imgPost)
                    binding.txtTitle.text = postTitle
                    binding.txtContent.text = postContent
                    binding.txtPrice.text = getString(R.string.str_post_price_won, postPrice?.let { formatNumber(it.toInt()) })
                    binding.txtDate.text = postDate
                    binding.txtIsSoldOut.text = if (postIsSoldOut == true) getString(R.string.str_post_sold_out) else getString(R.string.str_post_for_sale)
                    binding.btnChat.isEnabled = postIsSoldOut == false
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.str_post_get_post_failed), Toast.LENGTH_SHORT).show()
                }
        }

        // Firestore에서 유저 정보 불러오기
        sellerUID?.let {
            firebaseDB.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    sellerProfileImage = document.getString("profileImage")
                    sellerNickName = document.getString("nickname")

                    Glide.with(this).load(sellerProfileImage).into(binding.imgProfile)
                    binding.txtNickname.text = sellerNickName
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.str_post_get_user_failed), Toast.LENGTH_SHORT).show()
                }
        }

        // 로그인 되어있는 계정과 게시글 업로드한 계정 비교
        if (sellerUID == currentUserID) {
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnChat.visibility = View.INVISIBLE
        } else {
            binding.btnEdit.visibility = View.INVISIBLE
            binding.btnChat.visibility = View.VISIBLE
        }
    }

    // 위젯 클릭 리스너 함수
    private fun initWidget() {
        // 게시글 수정 버튼
        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this, ActivityPostEdit::class.java).apply {
                putExtra("postImage", postImage)
                putExtra("postTitle", postTitle)
                putExtra("postContent", postContent)
                putExtra("postPrice", postPrice?.toInt())
                putExtra("postIsSoldOut", postIsSoldOut)
                putExtra("postID", postID)
            })
        }

        // 쪽지 보내기 버튼
        binding.btnChat.setOnClickListener {
            startActivity(Intent(this, ActivityChatSend::class.java).apply {
                putExtra("sellerProfileImage", sellerProfileImage)
                putExtra("postTitle", postTitle)
                putExtra("sellerUID", sellerUID)
                putExtra("sellerNickName", sellerNickName)
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