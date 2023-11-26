package com.secondhand.trade

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.secondhand.trade.databinding.CustomDialogBinding

class CustomDialog(private val message: String?, private val onConfirm: () -> Unit, private val onCancel: () -> Unit) : DialogFragment() {
    private val binding by lazy { CustomDialogBinding.inflate(layoutInflater) }

    private val txtMessage by lazy { binding.txtMessage }
    private val txtCancel by lazy { binding.txtCancel }
    private val txtConfirm by lazy { binding.txtConfirm }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // 취소
        txtCancel.setOnClickListener {
            onCancel.invoke()
            dismiss()
        }

        // 확인
        txtConfirm.setOnClickListener {
            onConfirm.invoke()
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 다이얼로그 기본값 설정
        dialog?.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 해서 둥근 모서리 적용
            window?.attributes?.width = (context.resources.displayMetrics.widthPixels.times(0.85)).toInt() // 가로 길이를 화면의 85%로 설정
            setCanceledOnTouchOutside(false) // 다이얼로그 뒤쪽 클릭해도 다이얼로그가 닫히지 않게 설정
        }

        txtMessage.text = message
    }
}