package com.secondhand.trade

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.secondhand.trade.FunComp.Companion.formatNumber
import com.secondhand.trade.databinding.DialogHomeBinding

class DialogHome(private val onApply: (minValue: Int, maxValue: Int?, forSale: Boolean, soldOut: Boolean) -> Unit, private val onCancel: () -> Unit) : DialogFragment() {
    private lateinit var binding: DialogHomeBinding
    private val viewModel by activityViewModels<DialogHomeViewModel>()
    private val editPriceMin by lazy { binding.editPriceMin }
    private val editPriceMax by lazy { binding.editPriceMax }
    private val tgbtnForSale by lazy { binding.tgbtnForSale }
    private val tgbtnSoldOut by lazy { binding.tgbtnSoldOut }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogHomeBinding.inflate(inflater, container, false)

        /*
            minValue 초기값 0
            maxValue 초기값 null → null일 경우 최대값 제한 없음

            editPriceMin 공백 시 0으로 설정
            editPriceMax 공백 시 null로 설정

            minValue와 maxValue 값이 이미 설정되어 있을 경우 해당 값을 edittext에 입력
            ㄴ viewmodel 사용
        */

        // viewmodel에 저장된 값 불러와서 적용
        viewModel.minValue.observe(viewLifecycleOwner) { value ->
            editPriceMin.setText(formatNumber(value))
        }

        viewModel.maxValue.observe(viewLifecycleOwner) { value ->
            editPriceMax.setText(value?.let { formatNumber(it) })
        }

        viewModel.forSale.observe(viewLifecycleOwner) { value ->
            tgbtnForSale.isChecked = value
        }

        viewModel.soldOut.observe(viewLifecycleOwner) { value ->
            tgbtnSoldOut.isChecked = value
        }

        // 취소 버튼 클릭
        binding.btnCancel.setOnClickListener {
            onCancel.invoke()
            dismiss()
        }
        
        // 적용 버튼 클릭
        binding.btnApply.setOnClickListener {
            // edittext 값을 가져와서 쉼표 제거
            val strMinValue = editPriceMin.text.toString().replace(",", "")
            val strMaxValue = editPriceMax.text.toString().replace(",", "")

            // 쉼표 제거한 값을 int로 변환
            val minValue: Int = if (strMinValue.isEmpty()) 0 else strMinValue.toInt() // 값이 비어있으면 0으로 설정, 비어있지 않으면 int로 변환
            val maxValue: Int? = if (strMaxValue.isEmpty()) null else strMaxValue.toInt() // 값이 비어있으면 null로 설정, 비어있지 않으면 int로 변환

            // 판매 중, 판매 완료 선택 여부
            val forSale = binding.tgbtnForSale.isChecked 
            val soldOut = binding.tgbtnSoldOut.isChecked
            
            if (maxValue != null && minValue > maxValue) { // 최대 가격이 null이 아니고, 최소 가격이 최대 가격보다 높을 경우 경고 텍스트 출력
                binding.txtWarning.visibility = View.VISIBLE
            } else {
                // viewmodel에 값 저장
                viewModel.apply {
                    setMinValue(minValue)
                    setMaxValue(maxValue)
                    setForSale(forSale)
                    setSoldOut(soldOut)
                }
                // [최소 금액, 최대 금액, 판매 중, 판매 완료] 값 반환
                onApply.invoke(minValue, maxValue, forSale, soldOut)
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
            window?.attributes?.width = (context.resources.displayMetrics.widthPixels.times(0.9)).toInt() // 가로 길이를 화면의 90%로 설정
            setCanceledOnTouchOutside(false) // 다이얼로그 뒤쪽 클릭해도 다이얼로그가 닫히지 않게 설정
        }
        
        // TextWathcer 리스너 등록
        formatEdittext(editPriceMin)
        formatEdittext(editPriceMax)
    }

    // edittext 형식 설정 함수 (3자리마다 쉼표 추가, 맨 앞자리 0일 경우 뒤에 0 입력 방지)
    private fun formatEdittext(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                editText.removeTextChangedListener(this) // 무한루프 방지

                if (s.isNotEmpty()) {
                    val commaRemoved = s.toString().replace(",", "") // 쉼표 제거
                    // 최대 길이 제한
                    if (commaRemoved.length > 7) { // 7글자 초과 시
                        val shortened = commaRemoved.substring(0, 7) // 7자리까지 자르기
                        val numberFormatted = formatNumber(shortened.toInt()) // 3자리마다 쉼표 입력
                        editText.setText(numberFormatted)
                        editText.setSelection(editText.text.length) // 커서를 맨 뒤로 이동
                    } else {
                        if (s.toString().startsWith("00")) { // 앞자리가 0일 경우 0 연속으로 입력 방지
                            editText.setText("0")
                            editText.setSelection(editText.text.length)
                        } else {
                            val parsed = commaRemoved.toIntOrNull()
                            val formatted = parsed?.let { formatNumber(it) } // 3자리마다 쉼표 입력
                            editText.setText(formatted)
                            editText.setSelection(editText.text.length) // 커서를 맨 뒤로 이동
                        }
                    }
                }

                editText.addTextChangedListener(this)
            }
        })
    }
}