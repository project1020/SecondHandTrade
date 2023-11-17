package com.secondhand.trade

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ActivityFixArticle : AppCompatActivity() {
    private val fixTitle by lazy { findViewById<EditText>(R.id.editTitleFix) }
    private val fixContent by lazy { findViewById<EditText>(R.id.editContentFix) }
    private val fixPrice by lazy { findViewById<EditText>(R.id.editPriceFix) }
    private val fixSoldOut by lazy { findViewById<CheckBox>(R.id.isSoldOutFix) }
    //private val fixdate by lazy { findViewById<EditText>(R.id.editDateFix) }
    private val userIdFix by lazy { findViewById<TextView>(R.id.userIDFix) }
    //private val imageSelect by lazy { findViewById<ImageView>(R.id.photoImageView) }
    private val firestore = FirebaseFirestore.getInstance()
    private val board = firestore.collection("board_test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fix_article)

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val price = intent.getIntExtra("price", 0)
        //val date = intent.getSerializableExtra("date") as? Timestamp
        val image = intent.getStringExtra("image")
        val isSoldOut = intent.getBooleanExtra("isSoldOut", false)
        val userID = intent.getStringExtra("userID")
        val id = intent.getStringExtra("id")

        fixTitle.setText(title.toString())
        fixContent.setText(content.toString())
        fixPrice.setText(price.toString())
        fixSoldOut.isChecked = isSoldOut
        //fixdate.setText(date.toString())
        userIdFix.text = userID

        findViewById<Button>(R.id.btnModify).setOnClickListener {
            updateBoard()

        }

    }

    private fun updateBoard() {    // db에 값을 수정하는 함수
        val title = fixTitle.text.toString()
        val content = fixContent.text.toString()
        val price = fixPrice.text.toString().toInt()
        val isSoldOut = fixSoldOut.isChecked
        val userId = userIdFix.text.toString()
        val id = intent.getStringExtra("id")


        val dbMap = hashMapOf(
            "title" to title,
            "price" to price,
            "content" to content,
            "isSoldOut" to isSoldOut,
            //"imageUrl" to imageurl
            "userID" to userId
        )
        board.document(id.toString()).update(dbMap as Map<String, Any>)
            .addOnSuccessListener {
                finish()
            }.addOnFailureListener {  }
    }


}