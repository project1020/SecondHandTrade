package com.secondhand.trade

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ActivityPostRegister : AppCompatActivity() {
    private val editTitle by lazy { findViewById<EditText>(R.id.editTitle) }
    private val editContent by lazy { findViewById<EditText>(R.id.editContent) }
    private val editPrice by lazy { findViewById<EditText>(R.id.editPrice) }
    private val imageAddButton by lazy { findViewById<Button>(R.id.imageAddButton) }
    private val photoImageView by lazy { findViewById<ImageView>(R.id.photoImageView) }
    private val firestore = FirebaseFirestore.getInstance()
    private val board = firestore.collection("board_test")

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private var isImageUploaded = false
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        imageAddButton.setOnClickListener {
            openGallery()
        }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            if (selectedImageUri != null) {
                uploadImage(selectedImageUri!!)
            } else {
                addBoard()
                finish()
            }

        }
    }

    private fun addBoard() {
        val title = editTitle.text.toString()
        val content = editContent.text.toString()
        val price = editPrice.text.toString().toInt()

        val dbMap = hashMapOf(
            "title" to title,
            "price" to price,
            "content" to content,
            "imageUrl" to imageUrl
        )
        board.add(dbMap)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
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
                photoImageView.setImageURI(imageUri)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("image_product/${UUID.randomUUID()}.jpg")

        imagesRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    imageUrl = uri.toString()
                    isImageUploaded = true
                    if (isImageUploaded) {
                        addBoard()
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    // URL 가져오기 실패
                }
            }
            .addOnFailureListener { exception ->
                // 이미지 업로드 실패
            }
    }
}