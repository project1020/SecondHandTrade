package com.secondhand.trade

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat

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

        fun whitePlaceHolderForGlide(context: Context, width: Int, height: Int): BitmapDrawable {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            return BitmapDrawable(context.resources, bitmap)
        }
    }
}