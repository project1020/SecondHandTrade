package com.secondhand.trade

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
//import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.*

data class Articles (
    var title: String?= null,
    var content: String?= null,
    var price: Int?= null,
    //var isSoldOut: Boolean?= null
) {
    constructor(doc: QueryDocumentSnapshot) : this(
        title = doc.getString("title"),
        content = doc.getString("content"),
        price = doc.getLong("price")?.toInt(),
        //isSoldOut = doc.getBoolean("isSoldOut")
    )
}

class ActivityArticle : AppCompatActivity() {
    private val editTitle by lazy { findViewById<EditText>(R.id.editTitle) }
    private val editContent by lazy { findViewById<EditText>(R.id.editContent) }
    private val editPrice by lazy { findViewById<EditText>(R.id.editPrice) }
    //private val isSoldOut by lazy { findViewById<CheckBox>(R.id.isSoldOutFix) }
    //private val imageSelect by lazy { findViewById<ImageView>(R.id.photoImageView) }
    private val firestore = FirebaseFirestore.getInstance()
    private val board = firestore.collection("board_test")
    // db 연결, 및 ui 연동

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)


        findViewById<Button>(R.id.btnSubmit).setOnClickListener{
            addBoard()
            // 입력한 정보를 보관한 상태로 홈 화면으로 돌아가야 함

        }

    }

    private fun addBoard() {    // db에 값을 등록하는 함수
        val title = editTitle.text.toString()
        val content = editContent.text.toString()
        val price = editPrice.text.toString().toInt()
        //val isSoldOut = isSoldOut.isChecked

        val itemMap = hashMapOf(
            "title" to title,
            "price" to price,
            "content" to content,
            //"isSoldOut" to isSoldOut,
            //"imageUrl" to imageurl
        )
        board.add(itemMap)
                .addOnSuccessListener {
                    finish()
                }.addOnFailureListener {  }

    }






}