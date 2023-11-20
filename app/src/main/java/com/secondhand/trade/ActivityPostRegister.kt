package com.secondhand.trade

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ActivityPostRegister : AppCompatActivity() {
    private val editTitle by lazy { findViewById<EditText>(R.id.editTitle) }
    private val editContent by lazy { findViewById<EditText>(R.id.editContent) }
    private val editPrice by lazy { findViewById<EditText>(R.id.editPrice) }
    private val imageView by lazy { findViewById<ImageView>(R.id.photoImageView) }
    private val firestore = FirebaseFirestore.getInstance()
    private val board = firestore.collection("board_test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        findViewById<Button>(R.id.imageAddButton).setOnClickListener {
            displayImage()
        }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            addBoard()
            finish()
        }
    }

    private fun addBoard() {
        val title = editTitle.text.toString()
        val content = editContent.text.toString()
        val price = editPrice.text.toString().toInt()

        val dbMap = hashMapOf(
            "title" to title,
            "price" to price,
            "content" to content
        )
        board.add(dbMap)
            .addOnSuccessListener {
            }
            .addOnFailureListener { }
    }

    private fun displayImage() {
        val storageRef = FirebaseStorage.getInstance("gs://secondhandtrade-e2a57.appspot.com").reference
        val imageRef = storageRef.child("image_product")

        val ONE_MEGABYTE = 1024 * 1024.toLong()
        imageRef.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
            }
            .addOnFailureListener { exception ->
                // 이미지 다운로드 실패 시 처리할 내용을 작성합니다.
            }
    }
}
