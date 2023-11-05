package com.secondhand.trade

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.secondhand.trade.FunComp.Companion.formatNumber
import com.secondhand.trade.databinding.RowHomeBinding

data class DataHome(
    val image : String,
    val title : String,
    val date : String,
    val price : Int,
    val isSoldOut : Boolean
)

class AdapterHome(private val context: Context) : RecyclerView.Adapter<AdapterHome.ViewHolder>() {
    var itemList = mutableListOf<DataHome>()
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowHomeBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
        holder.itemView.setOnClickListener {
            onItemClickListener?.onClick(it, position)
        }
        holder.itemView.tag = holder
    }

    inner class ViewHolder(private val binding: RowHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DataHome) {
            Glide.with(itemView).load(item.image).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgProduct)
            binding.txtProduct.text = item.title
            binding.txtPrice.text = "${formatNumber(item.price)}원"
            binding.txtDate.text = item.date
            if (item.isSoldOut) {
                binding.viewStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
            } else {
                binding.viewStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }
}