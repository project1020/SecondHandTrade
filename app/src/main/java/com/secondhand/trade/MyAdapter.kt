package com.secondhand.trade

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.secondhand.trade.databinding.ItemLayoutBinding

data class Item(val sender: String, val text: String) {
    // 생성자에서 필요한 값들을 받아와서 초기화합니다.
    constructor(doc: QueryDocumentSnapshot) : this(doc.getString("sender") ?: "", doc.getString("message") ?: "")

    constructor(map: Map<*, *>) :
            this(map["sender"].toString(), map["text"].toString())
}

class MyViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

class MyAdapter(private val context: Context, private var items: List<Item>)
    : RecyclerView.Adapter<MyViewHolder>() {

    fun interface OnItemClickListener {
        fun onItemClick(student_id: String)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun updateList(newList: List<Item>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemLayoutBinding = ItemLayoutBinding.inflate(inflater, parent, false)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]
        holder.binding.sender.text = item.sender
        holder.binding.message.text = item.text

    }

    override fun getItemCount() = items.size
}