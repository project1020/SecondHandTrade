package com.secondhand.trade

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ActivityPostEdit : AppCompatActivity() {
    private val editTitleFix by lazy { findViewById<EditText>(R.id.editTitleFix) }
    private val editContentFix by lazy { findViewById<EditText>(R.id.editContentFix) }
    private val editPriceFix by lazy { findViewById<EditText>(R.id.editPriceFix) }
    private val isSoldOutFix by lazy { findViewById<CheckBox>(R.id.isSoldOutFix) }
    //private val fixdate by lazy { findViewById<EditText>(R.id.editDateFix) }
    private val userIDFix by lazy { findViewById<TextView>(R.id.userIDFix) }
    private val editImage by lazy { findViewById<ImageView>(R.id.editImageView) }
    private val firestore = FirebaseFirestore.getInstance()
    private val board = firestore.collection("board_test")
    private var imageUrl = intent.getStringExtra("imageUrl")
    private val storage = FirebaseStorage.getInstance()
    private val imageRef = storage.reference.child("images")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_edit)

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val price = intent.getIntExtra("price", 0)
        //val date = intent.getSerializableExtra("date") as? Timestamp
        val imageUrl = intent.getStringExtra("imageUrl")
        val isSoldOut = intent.getBooleanExtra("isSoldOut", false)
        val userID = intent.getStringExtra("userID")
        val id = intent.getStringExtra("id")

        editTitleFix.setText(title.toString())
        editContentFix.setText(content.toString())
        editPriceFix.setText(price.toString())
        isSoldOutFix.isChecked = isSoldOut
        //fixdate.setText(date.toString())
        userIDFix.text = userID

        findViewById<Button>(R.id.btnModify).setOnClickListener {
            updateBoard()
        }

    }
    
    private fun updateBoard() {    // db에 값을 수정하는 함수
        val title = editTitleFix.text.toString()
        val content = editContentFix.text.toString()
        val price = editPriceFix.text.toString().toInt()
        val isSoldOut = isSoldOutFix.isChecked
        val userId = userIDFix.text.toString()
        val id = intent.getStringExtra("id")


        val dbMap = hashMapOf(
            "title" to title,
            "price" to price,
            "content" to content,
            "isSoldOut" to isSoldOut,
            "imageUrl" to imageUrl,
            "userID" to userId
        )
        board.document(id.toString()).update(dbMap as Map<String, Any>)
            .addOnSuccessListener {
                finish()
            }.addOnFailureListener {  }
    }

    private fun displayImage(imageUrl: String) {
        val imageStorageRef = imageRef.child(imageUrl)
        imageStorageRef.downloadUrl
            .addOnSuccessListener { uri ->
                val options = RequestOptions()

                Glide.with(this)
                    .load(uri)
                    .apply(options)
                    .into(editImage)
            }
            .addOnFailureListener {
            }
    }

}