package com.secondhand.trade

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class ActivityPostEdit : AppCompatActivity() {
    private val editTitleFix by lazy { findViewById<EditText>(R.id.editTitleFix) }
    private val editContentFix by lazy { findViewById<EditText>(R.id.editContentFix) }
    private val editPriceFix by lazy { findViewById<EditText>(R.id.editPriceFix) }
    private val isSoldOutFix by lazy { findViewById<CheckBox>(R.id.isSoldOutFix) }
    //private val fixdate by lazy { findViewById<EditText>(R.id.editDateFix) }
    private val userIDFix by lazy { findViewById<TextView>(R.id.userIDFix) }
    private val editImage by lazy {findViewById<ImageView>(R.id.editImageView)}

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val board = firestore.collection("board_test")
    private var isImageUploaded = false
    private var image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_edit)

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val price = intent.getIntExtra("price", 0)
        //val date = intent.getSerializableExtra("date") as? Timestamp
        val isSoldOut = intent.getBooleanExtra("isSoldOut", false)
        val userID = intent.getStringExtra("userID")
        val id = intent.getStringExtra("id")
        val image = intent.getStringExtra("image")

        editTitleFix.setText(title.toString())
        editContentFix.setText(content.toString())
        editPriceFix.setText(price.toString())
        isSoldOutFix.isChecked = isSoldOut
        //fixdate.setText(date.toString())
        userIDFix.text = userID

        val view = findViewById<ImageView>(R.id.editImageView)
        val imageRef = Firebase.storage.getReferenceFromUrl("$image")
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener{
        }

        findViewById<Button>(R.id.imageEditButton).setOnClickListener {
            openGallery()
        }

        findViewById<Button>(R.id.btnModify).setOnClickListener {
            if (selectedImageUri != null) {
                uploadImage(selectedImageUri!!)
            } else {
                updateBoard()
                finish()
            }
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
            "image" to image,
            "userID" to userId
        )
        board.document(id.toString()).update(dbMap as Map<String, Any>)
            .addOnSuccessListener {
                finish()
            }.addOnFailureListener {  }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                selectedImageUri = imageUri
                editImage.setImageURI(imageUri)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("image_product/${UUID.randomUUID()}.jpg")

        imagesRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    image = uri.toString()
                    isImageUploaded = true
                    if (isImageUploaded) {
                        updateBoard()
                        finish()
                    }
                }.addOnFailureListener {
                }
            }
            .addOnFailureListener {
            }
    }
}