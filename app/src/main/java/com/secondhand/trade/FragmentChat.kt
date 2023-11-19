package com.secondhand.trade

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.FragmentChatBinding

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
        updateList() //받은 메세지들 출력
        return binding.root
    }

    //받은 메세지들 출력하는 메소드
    private fun updateList() {
        receivedMessagesCollectionRef.get().addOnSuccessListener {
            val items = mutableListOf<Item>()
            for (doc in it) {
                items.add(Item(doc))
            }
            adapter?.updateList(items)
        }
    }
    /*
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

     */
}