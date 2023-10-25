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

class FunComp {
    companion object {
        // 로그아웃 함수
        fun logout(activity: Activity, onLogoutSuccess: (() -> Unit)? = null, onLogoutFailed: (() -> Unit)? = null) {
            val userEmail = Firebase.auth.currentUser?.email
            if (userEmail != null) {
                // 로그인 상태를 false로 변경
                FirebaseFirestore.getInstance().collection("users").document(userEmail).update("isLoggedIn", false)
                    .addOnSuccessListener { // isLoggedIn 상태 변경 성공
                        Firebase.auth.signOut()
                        onLogoutSuccess?.invoke()
                    }
                    .addOnFailureListener { // isLoggedIn 상태 변경 실패
                        Toast.makeText(activity, "로그아웃 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        onLogoutFailed?.invoke()
                    }
            }
        }
    }
}