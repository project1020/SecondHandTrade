package com.secondhand.trade

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.secondhand.trade.databinding.FragmentChatBinding

data class DataMessage(
    val senderUID: String?,
    val message: String?,
    val date: Timestamp?
)

class FragmentChat : Fragment() {
    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }
    private lateinit var mainActivity: ActivityMain

    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var chatAdapter: AdapterChat
    private val currentUserID by lazy { Firebase.auth.currentUser?.uid }

    private var lastItem: DocumentSnapshot? = null
    private var isLoading = false
    private var isLastPage = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as ActivityMain
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.swipeChat.apply {
            setColorSchemeResources(R.color.colorPrimary)
            setOnRefreshListener {
                chatAdapter.itemList.clear()
                initItemList()
            }
        }

        initRecyclerView()
        initItemList()
        return binding.root
    }

    private fun initRecyclerView() {
        chatAdapter = AdapterChat(mainActivity, binding.recyclerChat)

        binding.recyclerChat.apply {
            adapter = chatAdapter
            setHasFixedSize(true)
            itemAnimator = null
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    // 맨 아래로 스크롤 시 다음 목록 가져오기
                    val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()
                    val itemTotalCount = recyclerView.adapter?.itemCount?.minus(1)
                    if (lastVisibleItemPosition == itemTotalCount) {
                        // 다음 목록 가져오기
                        if (!isLoading && !isLastPage) {
                            loadNextItemList()
                        }
                    }
                }
            })
        }

        chatAdapter.setOnItemClickListener { item, _ ->

        }
    }

    private fun initItemList() {
        binding.swipeChat.isRefreshing = true
        isLastPage = false

        currentUserID?.let { userID ->
            db.collection("chats").document(userID).collection("receivedmessage").orderBy("date", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener { documents ->
                val itemList = mutableListOf<DataChat>()
                val tasks = mutableListOf<Task<DocumentSnapshot>>()
                val messageList = documents.map { document ->
                    val senderUID = document.getString("sender")
                    val message = document.getString("message")
                    val date = document.getTimestamp("date")
                    DataMessage(senderUID, message, date)
                }

                messageList.forEach { message ->
                    message.senderUID?.let {
                        val task = db.collection("users").document(it).get()
                        tasks.add(task)
                    }
                }

                Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { document ->
                    val messageMap = document.associateBy { it.id }

                    messageList.forEach { message ->
                        val user = messageMap[message.senderUID]
                        val dataChat = DataChat(
                            profileImage = user?.getString("profileImage"),
                            nickname = user?.getString("nickname"),
                            message = message.message,
                            date = message.date,
                        )
                        itemList.add(dataChat)
                    }

                    chatAdapter.itemList.clear()
                    chatAdapter.itemList.addAll(itemList)
                    chatAdapter.notifyDataSetChanged()
                    binding.recyclerChat.scrollToPosition(0)
                    if (documents.size() > 0) lastItem = documents.documents[documents.size() - 1]
                    binding.swipeChat.isRefreshing = false
                }
            }
        }
    }

    private fun loadNextItemList() {
        if (isLoading) return
        isLoading = true
        chatAdapter.setLoading(true)

        lastItem?.let { documentSnapshot ->
            currentUserID?.let { userID ->
                db.collection("chats").document(userID).collection("receivedmessage").orderBy("date", Query.Direction.DESCENDING).startAfter(documentSnapshot).limit(10).get().addOnSuccessListener { documents ->
                    val itemList = mutableListOf<DataChat>()
                    val startPosition = chatAdapter.itemList.size
                    val messageList = documents.map { document ->
                        val senderUID = document.getString("sender")
                        val message = document.getString("message")
                        val date = document.getTimestamp("date")
                        DataMessage(senderUID, message, date)
                    }

                    val userTasks = messageList.mapNotNull { message ->
                        message.senderUID?.let { db.collection("users").document(it).get() }
                    }

                    Tasks.whenAllSuccess<DocumentSnapshot>(userTasks).addOnSuccessListener { document ->
                        val messageMap = document.associateBy { it.id }

                        messageList.forEach { message ->
                            val user = messageMap[message.senderUID]
                            val dataChat = DataChat(
                                profileImage = user?.getString("profileImage"),
                                nickname = user?.getString("nickname"),
                                message = message.message,
                                date = message.date,
                            )
                            itemList.add(dataChat)
                        }

                        chatAdapter.itemList.addAll(itemList)
                        chatAdapter.notifyItemRangeInserted(startPosition, itemList.size)
                        if (documents.size() > 0) lastItem = documents.documents[documents.size() - 1]
                        if (documents.size() < 10) isLastPage = true
                        isLoading = false
                        chatAdapter.setLoading(false)
                    }
                }
            }
        }
    }
}