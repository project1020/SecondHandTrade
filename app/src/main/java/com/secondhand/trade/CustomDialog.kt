package com.secondhand.trade

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.RangeSlider
import com.secondhand.trade.databinding.DialogHomeBinding
import java.text.NumberFormat
import java.util.Locale

class CustomDialog(private val onApply: (minValue: Int, maxValue: Int, forSale: Boolean, soldOut: Boolean) -> Unit, private val onCancel: () -> Unit) : DialogFragment() {
    private lateinit var binding: DialogHomeBinding
    private val sliderPrice by lazy { binding.sliderPrice }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogHomeBinding.inflate(inflater, container, false)

        binding.btnCancel.setOnClickListener {
            onCancel.invoke()
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            val (minValue, maxValue) = getSliderValues(sliderPrice)
            val forSaleState = binding.tgbtnForSale.isChecked
            val soldOutState = binding.tgbtnSoldOut.isChecked
            onApply.invoke(minValue, maxValue, forSaleState, soldOutState)
            dismiss()
        }

        sliderPrice.addOnChangeListener { _, _, _ ->
            setTextPrice()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 둥근 모서리 적용을 위한 배경 투명화
        dialog?.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
        }

        // 초기값 설정
        setTextPrice()
    }

    private fun getSliderValues(slider: RangeSlider): Pair<Int, Int> {
        val values = slider.values
        return values[0].toInt() to values[1].toInt()
    }

    private fun setTextPrice() {
        val (minValue, maxValue) = getSliderValues(sliderPrice)
        val startValue = NumberFormat.getNumberInstance(Locale.US).format(minValue)
        val endValue = NumberFormat.getNumberInstance(Locale.US).format(maxValue)

        with(binding) {
            txtPriceMin.text = getString(R.string.str_customdialog_text_price, startValue)
            txtPriceMax.text = getString(R.string.str_customdialog_text_price, endValue)
        }
    }
}