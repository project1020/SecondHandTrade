package com.secondhand.trade

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.secondhand.trade.FunComp.Companion.getTimeAgo
import com.secondhand.trade.databinding.FragmentChatBinding
import android.util.Pair

data class DataMessage(
    val title: String?,
    val message: String?,
    val date: Timestamp?,
    val senderUID: String?
)

class FragmentChat : Fragment() {
    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }
    private lateinit var mainActivity: ActivityMain

    private val firebaseDB by lazy { FirebaseFirestore.getInstance() }

    private val swipeChat by lazy { binding.swipeChat }
    private val recyclerChat by lazy { binding.recyclerChat }

    private val currentUserUID by lazy { Firebase.auth.currentUser?.uid }

    private lateinit var chatAdapter: AdapterChat

    private var lastItem: DocumentSnapshot? = null
    private var isLoading = false
    private var isLastPage = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as ActivityMain
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        swipeChat.apply {
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
        chatAdapter = AdapterChat(mainActivity, recyclerChat)

        recyclerChat.apply {
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

        chatAdapter.setOnItemClickListener { item, position ->
            val itemView = binding.recyclerChat.findViewHolderForAdapterPosition(position)?.itemView

            val imgProfile = itemView?.findViewById<ImageView>(R.id.imgProfile) // R.id.imgProfile는 실제 뷰의 ID로 변경해야 함
            val txtMessage = itemView?.findViewById<TextView>(R.id.txtMessage)
            val txtDate = itemView?.findViewById<TextView>(R.id.txtDate)
            val txtNickname = itemView?.findViewById<TextView>(R.id.txtNickname)

            val transitionImageProfile = Pair.create<View, String>(imgProfile, "transitionImgProfile")
            val transitionTxtMessage = Pair.create<View, String>(txtMessage, "transitionTxtMessage")
            val transitionTxtDate = Pair.create<View, String>(txtDate, "transitionTxtDate")
            val transitionTxtNickname = Pair.create<View, String>(txtNickname, "transitionTxtNickname")

            val options  = ActivityOptions.makeSceneTransitionAnimation(mainActivity, transitionImageProfile, transitionTxtMessage, transitionTxtDate, transitionTxtNickname).toBundle()
            startActivity(Intent(mainActivity, ActivityChatReceive::class.java).apply {
                putExtra("profileImage", item.profileImage)
                putExtra("chatTitle", item.title)
                putExtra("chatMessage", item.message)
                putExtra("chatDate", getTimeAgo(item.date?.toDate()))
                putExtra("senderNickname", item.nickname)
            }, options)
        }
    }

    private fun initItemList() {
        swipeChat.isRefreshing = true
        isLastPage = false

        currentUserUID?.let { userID ->
            firebaseDB.collection("chats").document(userID).collection("receivedmessage").orderBy("date", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener { documents ->
                val itemList = mutableListOf<DataChat>()
                val tasks = mutableListOf<Task<DocumentSnapshot>>()
                val messageList = documents.map { document ->
                    val title = document.getString("title")
                    val message = document.getString("message")
                    val date = document.getTimestamp("date")
                    val senderUID = document.getString("sender")
                    DataMessage(title, message, date, senderUID)
                }

                messageList.forEach { message ->
                    message.senderUID?.let {
                        val task = firebaseDB.collection("users").document(it).get()
                        tasks.add(task)
                    }
                }

                Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { document ->
                    val messageMap = document.associateBy { it.id }

                    messageList.forEach { message ->
                        val user = messageMap[message.senderUID]
                        val dataChat = DataChat(
                            profileImage = user?.getString("profileImage"),
                            title = message.title,
                            message = message.message,
                            date = message.date,
                            nickname = user?.getString("nickname")
                        )
                        itemList.add(dataChat)
                    }

                    chatAdapter.itemList.clear()
                    chatAdapter.itemList.addAll(itemList)
                    chatAdapter.notifyDataSetChanged()
                    recyclerChat.scrollToPosition(0)
                    if (documents.size() > 0) lastItem = documents.documents[documents.size() - 1]
                    swipeChat.isRefreshing = false
                }
            }
        }
    }

    private fun loadNextItemList() {
        if (isLoading) return
        isLoading = true
        chatAdapter.setLoading(true)

        lastItem?.let { documentSnapshot ->
            currentUserUID?.let { userID ->
                firebaseDB.collection("chats").document(userID).collection("receivedmessage").orderBy("date", Query.Direction.DESCENDING).startAfter(documentSnapshot).limit(10).get().addOnSuccessListener { documents ->
                    val itemList = mutableListOf<DataChat>()
                    val startPosition = chatAdapter.itemList.size
                    val messageList = documents.map { document ->
                        val title = document.getString("title")
                        val message = document.getString("message")
                        val date = document.getTimestamp("date")
                        val senderUID = document.getString("sender")
                        DataMessage(title, message, date, senderUID)
                    }

                    val userTasks = messageList.mapNotNull { message ->
                        message.senderUID?.let { firebaseDB.collection("users").document(it).get() }
                    }

                    Tasks.whenAllSuccess<DocumentSnapshot>(userTasks).addOnSuccessListener { document ->
                        val messageMap = document.associateBy { it.id }

                        messageList.forEach { message ->
                            val user = messageMap[message.senderUID]
                            val dataChat = DataChat(
                                profileImage = user?.getString("profileImage"),
                                title = message.title,
                                message = message.message,
                                date = message.date,
                                nickname = user?.getString("nickname")
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