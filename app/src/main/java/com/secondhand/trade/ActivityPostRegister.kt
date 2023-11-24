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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.secondhand.trade.FunComp.Companion.formatEdittext
import com.secondhand.trade.databinding.ActivityPostRegisterBinding
import java.util.Date
import java.util.UUID

class ActivityPostRegister : AppCompatActivity() {
    private val binding by lazy { ActivityPostRegisterBinding.inflate(layoutInflater) }

    private val firebaseDB by lazy { FirebaseFirestore.getInstance() }
    private val firebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val currentUserID by lazy { Firebase.auth.currentUser?.uid }

    private val imgProduct by lazy { binding.imgProduct }
    private val editTitle by lazy { binding.editTitle }
    private val editContent by lazy { binding.editContent }
    private val editPrice by lazy { binding.editPrice }
    private val cardImage by lazy { binding.cardImage }
    private val imgAdd by lazy { binding.imgAdd }
    private val imgEdit by lazy { binding.imgEdit }
    private val progressIndicator by lazy { binding.progressRegister }

    private var imageURI: Uri? = null
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

        binding.toolbarRegister.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_postregister_toolbar_item, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menuRegister -> {
                        uploadImage()
                    }
                }
                return true
            }
        })

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                imageURI = it.data?.data
                imgAdd.visibility = View.GONE
                imgEdit.visibility = View.VISIBLE
                Glide.with(this).load(imageURI).into(imgProduct)
            }
        }

        editPrice.apply { formatEdittext(this) }

        cardImage.setOnClickListener {
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

    private fun registerPost() {
        val postMap = hashMapOf(
            "image" to imageURI,
            "title" to editTitle.text.toString(),
            "content" to editContent.text.toString(),
            "price" to editPrice.text.toString().replace(",", "").toInt(),
            "date" to Date(),
            "isSoldOut" to false,
            "userID" to currentUserID
        )

        firebaseDB.collection("board_test").add(postMap)
            .addOnSuccessListener {
                isUpdating = false
                progressIndicator.visibility = View.GONE
                Toast.makeText(this, "물품을 등록했습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                progressIndicator.visibility = View.GONE
                Toast.makeText(this, "물품 등록에 실패했습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
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

        imageURI?.let {
            storageRef.putFile(imageURI!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageURI = downloadUri
                        registerPost()
                    }
                }
        }
    }
}