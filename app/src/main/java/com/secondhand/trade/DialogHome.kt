package com.secondhand.trade

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.secondhand.trade.databinding.DialogHomeBinding

class DialogHome(private val onApply: () -> Unit, private val onCancel: () -> Unit) : DialogFragment() {
    private lateinit var binding: DialogHomeBinding
    private val viewModel by activityViewModels<HomeFilterViewModel>()
    private val tgbtnForSale by lazy { binding.tgbtnForSale }
    private val tgbtnSoldOut by lazy { binding.tgbtnSoldOut }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogHomeBinding.inflate(inflater, container, false)

        initViewModel()
        onToggleButtonChanged()

        // 취소 버튼 클릭
        binding.btnCancel.setOnClickListener {
            onCancel.invoke()
            dismiss()
        }
        
        // 적용 버튼 클릭
        binding.btnApply.setOnClickListener {
            // 판매 중, 판매 완료 선택 여부
            val forSale = binding.tgbtnForSale.isChecked 
            val soldOut = binding.tgbtnSoldOut.isChecked

            if (!forSale && !soldOut) {
                binding.txtWarning.visibility = View.VISIBLE
            } else {
                viewModel.apply {
                    setForSale(forSale)
                    setSoldOut(soldOut)
                }

                onApply.invoke()
                dismiss()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 다이얼로그 기본값 설정
        dialog?.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 해서 둥근 모서리 적용
            window?.attributes?.width = (context.resources.displayMetrics.widthPixels.times(0.85)).toInt() // 가로 길이를 화면의 90%로 설정
            setCanceledOnTouchOutside(false) // 다이얼로그 뒤쪽 클릭해도 다이얼로그가 닫히지 않게 설정
        }
    }

    // ViewModel 값 변화 감지
    private fun initViewModel() {
        // viewmodel에 저장된 값 불러와서 적용
        viewModel.forSale.observe(viewLifecycleOwner) { value ->
            tgbtnForSale.isChecked = value
        }

        viewModel.soldOut.observe(viewLifecycleOwner) { value ->
            tgbtnSoldOut.isChecked = value
        }
    }

    // ToggleButton 상태 변화 감지
    private fun onToggleButtonChanged() {
        binding.tgbtnForSale.setOnCheckedChangeListener { _, _ ->
            binding.txtWarning.visibility = View.INVISIBLE
        }

        binding.tgbtnSoldOut.setOnCheckedChangeListener { _, _ ->
            binding.txtWarning.visibility = View.INVISIBLE
        }
    }
}