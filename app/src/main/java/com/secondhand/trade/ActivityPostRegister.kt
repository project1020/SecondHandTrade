package com.secondhand.trade

import android.app.Activity
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
import com.secondhand.trade.FunComp.Companion.clearErrorOnTextChangedAndFocus
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
    private val inputTitle by lazy { binding.inputTitle }
    private val inputContent by lazy { binding.inputContent }
    private val inputPrice by lazy { binding.inputPrice }
    private val txtError by lazy { binding.txtError }
    private val toolbarRegister by lazy { binding.toolbarRegister }
    private val progressIndicator by lazy { binding.progressRegister }

    private var imageURI: Uri? = null
    private var isUpdating = false

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    // 업로드 하는 중에는 뒤로가기 방지
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishRegister()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, callback)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                imageURI = it.data?.data
                imgAdd.visibility = View.GONE
                imgEdit.visibility = View.VISIBLE
                Glide.with(this).load(imageURI).into(imgProduct)
            }
        }

        initToolbar()
        initMenu()
        initWidget()
        editTextOnTextChangedAndFocus()
    }

    private fun initToolbar() {
        toolbarRegister.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun initMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_postregister_toolbar_item, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menuRegister -> {
                        val title = editTitle.text.toString()
                        val content = editContent.text.toString()
                        val price = editPrice.text.toString().replace(",", "")

                        if (isValidInput(imageURI, title, content, price)) {
                            uploadImage()
                        }
                    }
                }
                return true
            }
        })
    }

    private fun initWidget() {
        editPrice.apply { formatEdittext(this) }

        cardImage.setOnClickListener {
            txtError.visibility = View.GONE
            openGallery()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishRegister()
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

    private fun isValidInput(uri: Uri?, title: String, content: String, price: String): Boolean {
        txtError.visibility = if (uri == null) View.VISIBLE else View.GONE
        inputTitle.error = if (title.trim().isEmpty()) getString(R.string.str_postedit_input_title) else null
        inputContent.error = if (content.trim().isEmpty()) getString(R.string.str_postedit_input_content) else null
        inputPrice.error = if (price.trim().isEmpty()) getString(R.string.str_postedit_input_price) else null

        return uri != null && inputTitle.error == null && inputContent.error == null && inputPrice.error == null
    }

    private fun finishRegister() {
        if(!isUpdating) {
            CustomDialog(getString(R.string.str_postregister_cancel_register),
                onConfirm = {
                    setResult(Activity.RESULT_OK, Intent()).also { finish() }
                },
                onCancel = {}
            ).show(supportFragmentManager, "CustomDialog")
        } else {
            Toast.makeText(this, getString(R.string.str_postregister_is_registering), Toast.LENGTH_SHORT).show()
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

        firebaseDB.collection("board").add(postMap)
            .addOnSuccessListener {
                isUpdating = false
                progressIndicator.visibility = View.GONE
                Toast.makeText(this, getString(R.string.str_postregister_register_success), Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK, Intent()).also { finish() }
            }
            .addOnFailureListener {
                progressIndicator.visibility = View.GONE
                Toast.makeText(this, getString(R.string.str_postregister_register_failed), Toast.LENGTH_SHORT).show()
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
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.str_postregister_image_upload_failed), Toast.LENGTH_SHORT).show()
                }
        }
    }
}