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
import com.secondhand.trade.FunComp.Companion.clearErrorOnTextChangedAndFocus
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
    private val cardImage by lazy { binding.cardImage }
    private val btntoggleIsSoldOut by lazy { binding.btntoggleIsSoldOut }
    private val inputTitle by lazy { binding.inputTitle }
    private val inputContent by lazy { binding.inputContent }
    private val inputPrice by lazy { binding.inputPrice }
    private val toolbarEdit by lazy { binding.toolbarEdit }
    private val progressIndicator by lazy { binding.progressEdit }

    private val postImage by lazy { intent.getStringExtra("postImage") }
    private val postTitle by lazy { intent.getStringExtra("postTitle") }
    private val postContent by lazy { intent.getStringExtra("postContent") }
    private val postPrice by lazy { intent.getIntExtra("postPrice", 0) }
    private val postIsSoldOut by lazy { intent.getBooleanExtra("postIsSoldOut", false) }
    private val postID by lazy { intent.getStringExtra("postID") }

    private var imageURI: Uri? = null
    private var changedImageURI: String? = null
    private val isSoldOut by lazy { when (btntoggleIsSoldOut.checkedButtonId) {
        R.id.btnForSale -> false
        R.id.btnSoldOut -> true
        else -> false }
    }
    private var isUpdating = false

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    // 업로드 하는 중에는 뒤로가기 방지
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(!isUpdating)
                finishEdit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, callback)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                imageURI = it.data?.data
                Glide.with(this).load(imageURI).into(imgProduct)
            }
        }

        initToolbar()
        initMenu()
        initWidget()
        editTextOnTextChangedAndFocus()
    }

    private fun initToolbar() {
        toolbarEdit.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun initMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_postedit_toolbar_item, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menuEdit -> {
                        val title = editTitle.text.toString()
                        val content = editContent.text.toString()
                        val price = editPrice.text.toString().replace(",", "")

                        if (isValidInput(title, content, price)) {
                            uploadImage()
                        }
                    }
                }
                return true
            }
        })
    }

    private fun initWidget() {
        Glide.with(this).load(postImage).into(imgProduct)
        editTitle.setText(postTitle)
        editContent.setText(postContent)
        editPrice.apply {
            formatEdittext(this)
            setText(postPrice.toString())
        }
        btntoggleIsSoldOut.check(if (postIsSoldOut) R.id.btnSoldOut else R.id.btnForSale)

        cardImage.setOnClickListener {
            openGallery()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if(!isUpdating)
                    finishEdit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editTextOnTextChangedAndFocus() {
        editTitle.clearErrorOnTextChangedAndFocus(inputTitle)
        editContent.clearErrorOnTextChangedAndFocus(inputContent)
        editPrice.clearErrorOnTextChangedAndFocus(inputPrice)
    }

    private fun isValidInput(title: String, content: String, price: String): Boolean {
        inputTitle.error = if (title.trim().isEmpty()) getString(R.string.str_postedit_input_title) else null
        inputContent.error = if (content.trim().isEmpty()) getString(R.string.str_postedit_input_content) else null
        inputPrice.error = if (price.trim().isEmpty()) getString(R.string.str_postedit_input_price) else null

        return inputTitle.error == null && inputContent.error == null && inputPrice.error == null
    }

    private fun finishEdit() {
        if(!isUpdating) {
            CustomDialog(getString(R.string.str_postedit_cancel_edit),
                onConfirm = {
                    finish()
                },
                onCancel = {}
            ).show(supportFragmentManager, "CustomDialog")
        } else {
            Toast.makeText(this, getString(R.string.str_postedit_is_editing), Toast.LENGTH_SHORT).show()
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
            firebaseDB.collection("board").document(it).update(postMap as Map<String, Any>)
                .addOnSuccessListener {
                    isUpdating = false
                    progressIndicator.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.str_postedit_edit_success), Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    progressIndicator.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.str_postedit_edit_failed), Toast.LENGTH_SHORT).show()
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
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.str_postedit_image_upload_failed), Toast.LENGTH_SHORT).show()
                }
        } else {
            changedImageURI = postImage
            updatePost()
        }
    }
}