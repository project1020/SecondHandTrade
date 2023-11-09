package com.secondhand.trade

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.secondhand.trade.databinding.RowRegisterBinding

data class DataRegister(val image : String)

class AdapterRegister(private val context: Context) : RecyclerView.Adapter<AdapterRegister.ViewHolder>() {
    var itemList = mutableListOf<DataRegister>()
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowRegisterBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
        holder.itemView.setOnClickListener {
            onItemClickListener?.onClick(it, position)
        }
    }

    inner class ViewHolder(private val binding: RowRegisterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DataRegister) {
            Glide.with(itemView).load(item.image).diskCacheStrategy(DiskCacheStrategy.NONE).into(binding.imgProfile)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }
}