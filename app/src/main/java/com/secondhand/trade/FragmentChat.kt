package com.secondhand.trade

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.FragmentChatBinding
import com.secondhand.trade.databinding.ItemLayoutBinding

var userId = "userid"
class FragmentChat : Fragment() {
    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }
    private lateinit var mainActivity: ActivityMain
    private var adapter: MyAdapter? = null
    private val db: FirebaseFirestore = Firebase.firestore
    private var receivedMessagesCollectionRef = db.collection("chats").document(userId).collection("receivedmessage")
    private var snapshotListener: ListenerRegistration? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as ActivityMain
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Firebase.auth.currentUser?.let { user ->
            userId = user.uid
            receivedMessagesCollectionRef = db.collection("chats").document(userId).collection("receivedmessage")
        }
        adapter = MyAdapter(requireContext(), emptyList())
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.adapter = adapter  // 어댑터를 리사이클러뷰에 연결
        updateList()
        binding.button.setOnClickListener {
            addItem()
        }
        return binding.root
    }


    private fun updateList() {
        receivedMessagesCollectionRef.get().addOnSuccessListener {
            val items = mutableListOf<Item>()
            for (doc in it) {
                items.add(Item(doc))
            }
            adapter?.updateList(items)
        }
    }
    private fun addItem() {
        receivedMessagesCollectionRef = db.collection("chats").document(userId).collection("receivedmessage")
        Firebase.auth.currentUser?.let { user ->
            val userId = user.uid
            db.collection("users").document(userId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val name = task.result.getString("nickname")
                    val messageinput = binding.editTextText.text.toString()
                    val itemMap = hashMapOf(
                        "sender" to name,
                        "text" to messageinput
                    )
                    receivedMessagesCollectionRef.add(itemMap).addOnSuccessListener { updateList() }.addOnFailureListener {  }
                }
            }
        }

    }
}