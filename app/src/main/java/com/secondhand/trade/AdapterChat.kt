package com.secondhand.trade

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.Timestamp
import com.secondhand.trade.FunComp.Companion.getTimeAgo
import com.secondhand.trade.databinding.ProgressbarRecyclerviewBinding
import com.secondhand.trade.databinding.RowChatBinding

data class DataChat(
    val profileImage : String?,
    val title : String?,
    val message : String?,
    val date : Timestamp?,
    val nickname : String?
)

class AdapterChat(private val context: Context, private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var itemList = mutableListOf<DataChat>()
    private var onItemClickListener: ((DataChat, Int) -> Unit)? = null
    private var isLoading = false

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    override fun getItemCount(): Int {
        return if (itemList.size == 0) {
            0
        } else {
            itemList.size + 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val bindingChat = RowChatBinding.inflate(LayoutInflater.from(context), parent, false)
            ViewHolder(bindingChat)
        } else {
            val bindingProgress = ProgressbarRecyclerviewBinding.inflate(LayoutInflater.from(context), parent, false)
            LoadingViewHolder(bindingProgress)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(itemList[position])
        } else if (holder is LoadingViewHolder) {
            holder.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemList.size) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    inner class ViewHolder(private val binding: RowChatBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(itemList[position], position)
                }
            }
        }

        fun bind(item: DataChat) {
            Glide.with(itemView).load(item.profileImage).diskCacheStrategy(DiskCacheStrategy.NONE).into(binding.imgProfile)
            binding.txtNickname.text = item.nickname
            binding.txtMessage.text = item.message
            binding.txtDate.text = getTimeAgo(item.date?.toDate())
        }
    }

    inner class LoadingViewHolder(binding: ProgressbarRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root) {
        val progressBar: ProgressBar = binding.progressRecycler
    }

    fun setOnItemClickListener(listener: (DataChat, Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setLoading(isLoading: Boolean) {
        if (this.isLoading == isLoading) return // 상태 변경이 없으면 아무 것도 하지 않음

        this.isLoading = isLoading
        if (isLoading) {
            // 마지막 항목으로 스크롤
            val lastItemPosition = itemList.size - 1
            recyclerView.scrollToPosition(lastItemPosition)
            // progress bar 높이만큼 스크롤
            val progressBarHeight = recyclerView.height / 10
            recyclerView.smoothScrollBy(0, progressBarHeight)
            notifyItemInserted(itemList.size)
        } else {
            notifyItemRemoved(itemList.size)
        }
    }
}