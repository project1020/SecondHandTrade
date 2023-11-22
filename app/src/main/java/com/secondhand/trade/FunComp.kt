package com.secondhand.trade

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FunComp {
    companion object {
        // 로그아웃 함수
        fun logout(activity: Activity, onLogoutSuccess: (() -> Unit)? = null, onLogoutFailed: (() -> Unit)? = null) {
            val userId = Firebase.auth.currentUser?.uid
            if (userId != null) {
                // 로그인 상태를 false로 변경
                FirebaseFirestore.getInstance().collection("users").document(userId).update("isLoggedIn", false)
                    .addOnSuccessListener { // isLoggedIn 상태 변경 성공
                        Firebase.auth.signOut()
                        Preferences.isAutoLogin = false
                        onLogoutSuccess?.invoke()
                    }
                    .addOnFailureListener { // isLoggedIn 상태 변경 실패
                        Toast.makeText(activity, "로그아웃 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        onLogoutFailed?.invoke()
                    }
            }
        }

        // 1000단위 쉼표 입력 함수
        fun formatNumber(num: Int) : String {
            return NumberFormat.getInstance().format(num)
        }

        // Glide 이미지 표시 전 임시 배경을 흰색으로 설정하는 함수
        fun whitePlaceHolderForGlide(context: Context, width: Int, height: Int): BitmapDrawable {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            return BitmapDrawable(context.resources, bitmap)
        }

        // Glide 사용시 이미지 전환 애니메이션을 정상적으로 나타내기 위한 함수
        fun ImageView.transitionWithGlide(context: Context, url: String?, onLoadingFinished: () -> Unit = {}) {
            val listener = object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    onLoadingFinished()
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    onLoadingFinished()
                    return false
                }
            }

            Glide.with(this)
                .load(url)
                .apply(RequestOptions().dontTransform().placeholder(whitePlaceHolderForGlide(context, 10, 10)))
                .listener(listener)
                .into(this)
        }

        // 얼마 전에 올라온 글인지 글씨로 반환해주는 함수
        fun getTimeAgo(date: Date?): String {
            date?.let {
                // 현재 날짜와 게시글 날짜의 차이 계산
                val diff = Date().time - it.time

                // 시간 단위로 변환
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                
                // 날짜 형식 0000.00.00 으로 설정
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

                return when {
                    seconds < 60 -> "방금 전" // 60초 이내
                    minutes < 60 -> "${minutes}분 전" // 60분 이내
                    hours < 24 -> "${hours}시간 전" // 하루 이내
                    days < 7 -> "${days}일 전" // 1주일 이내
                    else -> dateFormat.format(it) // 그 외
                }
            }

            // 날짜가 null일 경우 빈 문자열 반환
            return ""
        }

        fun EditText.clearErrorOnTextChangedAndFocus(textInputLayout: TextInputLayout) {
            addTextChangedListener(onTextChanged = { _, _, _, _, -> textInputLayout.error = null })
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) textInputLayout.error = null }
        }
    }
}