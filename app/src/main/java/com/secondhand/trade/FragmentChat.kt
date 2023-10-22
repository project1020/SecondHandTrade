package com.secondhand.trade

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.secondhand.trade.databinding.FragmentChatBinding

class FragmentChat : Fragment() {
    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }
    private lateinit var mainActivity: ActivityMain

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as ActivityMain
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        return binding.root
    }
}