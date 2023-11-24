package com.secondhand.trade

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.secondhand.trade.FunComp.Companion.formatEdittext
import com.secondhand.trade.databinding.ActivityPostEditBinding
import java.util.Date
import java.util.UUID

class ActivityPostEdit : AppCompatActivity() {
    private val binding by lazy { ActivityPostEditBinding.inflate(layoutInflater) }

    private val firebaseDB by lazy { FirebaseFirestore.getInstance() }
    private val firebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val imgProduct by lazy { binding.imgProduct }
    private val editTitle by lazy { binding.editTitle }
    private val editContent by lazy { binding.editContent }
    private val editPrice by lazy { binding.editPrice }
    private val btntoggleIsSoldOut by lazy { binding.btntoggleIsSoldOut }
    private val progressIndicator by lazy { binding.progressEdit }

    private val postImage by lazy { intent.getStringExtra("postImage") }
    private val postTitle by lazy { intent.getStringExtra("postTitle") }
    private val postContent by lazy { intent.getStringExtra("postContent") }
    private val postPrice by lazy { intent.getIntExtra("postPrice", 0) }
    private val postIsSoldOut by lazy { intent.getBooleanExtra("postIsSoldOut", false) }
    private val postID by lazy { intent.getStringExtra("postID") }

    private var imageURI: Uri? = null
    private var changedImageURI: String? = null
    private val isSoldOut by lazy { when (binding.btntoggleIsSoldOut.checkedButtonId) {
        R.id.btnForSale -> false
        R.id.btnSoldOut -> true
        else -> false }
    }
    private var isUpdating = false

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(!isUpdating)
                finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, callback)

        binding.toolbarEdit.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_postedit_toolbar_item, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menuEdit -> {
                        uploadImage()
                    }
                }
                return true
            }
        })

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                imageURI = it.data?.data
                Glide.with(this).load(imageURI).into(imgProduct)
            }
        }

        Glide.with(this).load(postImage).into(imgProduct)
        editTitle.setText(postTitle)
        editContent.setText(postContent)
        editPrice.apply {
            formatEdittext(this)
            setText(postPrice.toString())
        }
        btntoggleIsSoldOut.check(if (postIsSoldOut) R.id.btnSoldOut else R.id.btnForSale)

        imgProduct.setOnClickListener {
            openGallery()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if(!isUpdating)
                    finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updatePost() {
        val postMap = hashMapOf(
            "image" to changedImageURI,
            "title" to editTitle.text.toString(),
            "content" to editContent.text.toString(),
            "price" to editPrice.text.toString().replace(",", "").toInt(),
            "date" to Date(),
            "isSoldOut" to isSoldOut,
        )

        postID?.let {
            firebaseDB.collection("board_test").document(it).update(postMap as Map<String, Any>)
                .addOnSuccessListener {
                    isUpdating = false
                    progressIndicator.visibility = View.GONE
                    Toast.makeText(this, "게시글을 수정했습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    progressIndicator.visibility = View.GONE
                    Toast.makeText(this, "게시글 수정에 실패했습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        activityResultLauncher.launch(intent)
    }

    private fun uploadImage() {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(imageURI.toString())
        val storageRef = firebaseStorage.reference.child("image_product/${UUID.randomUUID()}.$fileExtension")

        isUpdating = true
        progressIndicator.visibility = View.VISIBLE

        if (imageURI != null) {
            storageRef.putFile(imageURI!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        changedImageURI = downloadUri.toString()
                        updatePost()
                    }
                }
        } else {
            changedImageURI = postImage
            updatePost()
        }
    }
}