package com.secondhand.trade

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.secondhand.trade.databinding.FragmentHomeBinding

class FragmentHome : Fragment() {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var mainActivity: ActivityMain

    private val viewModel by activityViewModels<HomeFilterViewModel>()

    private val firebaseDB by lazy { FirebaseFirestore.getInstance() }

    private val toolbarHome by lazy { binding.toolbarHome }
    private val fabHome by lazy { binding.fabHome }
    private val swipeHome by lazy { binding.swipeHome }
    private val recyclerHome by lazy { binding.recyclerHome }
    private val txtNoProduct by lazy { binding.txtNoProduct }

    private lateinit var homeAdapter: AdapterHome
    private lateinit var searchMenuItem: MenuItem
    private lateinit var searchView: SearchView

    private var lastItem: DocumentSnapshot? = null
    private var searchQuery: String? = null
    private var isLoading = false
    private var isLastPage = false
    private var forSale = true
    private var soldOut = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as ActivityMain
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initToolbar()
        initMenu()
        initWidget()
        initViewModel()
        initRecyclerview()
        initItemList()

        return binding.root
    }
    
    private fun initToolbar() {
        mainActivity.setSupportActionBar(toolbarHome)
    }

    private fun initMenu() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home_toolbar_item, menu)
                searchMenuItem = menu.findItem(R.id.menuSearch)
                searchView = searchMenuItem.actionView as SearchView

                searchView.queryHint = getString(R.string.str_home_search_product)
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        searchQuery = query
                        initItemList()
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menuFilter -> {
                        // 필터 다이얼로그 띄우기
                        DialogHome(
                            onApply = {
                                initItemList()
                            },
                            onCancel = {}
                        ).show(childFragmentManager, "DialogHome")
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initWidget() {
        fabHome.setOnClickListener {
            startActivity(Intent(mainActivity, ActivityPostRegister::class.java))
        }
    }

    // ViewModel 값 변화 감지
    private fun initViewModel() {
        viewModel.forSale.observe(viewLifecycleOwner) { value ->
            forSale = value
        }

        viewModel.soldOut.observe(viewLifecycleOwner) { value ->
            soldOut = value
        }
    }

    // RecyclerView 설정
    private fun initRecyclerview() {
        homeAdapter = AdapterHome(mainActivity, recyclerHome)

        recyclerHome.apply {
            adapter = homeAdapter
            addItemDecoration(ItemDecoratorDividerPadding(5)) // 아이템 간격 설정
            setHasFixedSize(true) // 아이템 크기가 고정되어 있음을 명시
            itemAnimator = null // 아이템 변경 애니메이션 삭제 (깜빡임 방지)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0 && fabHome.isExtended) { // 위로 스크롤
                        fabHome.shrink() // FAB 축소
                    } else if (dy < 0 && !fabHome.isExtended) { // 아래로 스크롤
                        fabHome.extend() // FAB 확장
                    }

                    // 맨 아래로 스크롤 시 다음 목록 가져오기
                    val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()
                    val itemTotalCount = recyclerView.adapter?.itemCount?.minus(1)
                    if (lastVisibleItemPosition == itemTotalCount) {
                        // 다음 목록 가져오기
                        if (!isLoading && !isLastPage)
                            loadNextItemList()
                    }
                }
            })

            swipeHome.apply {
                setColorSchemeResources(R.color.colorPrimary)
                setOnRefreshListener {
                    searchQuery = null
                    homeAdapter.itemList.clear()
                    searchMenuItem.collapseActionView()
                    initItemList()
                }
            }

            homeAdapter.setOnItemClickListener { item, _ ->
                startActivity(Intent(mainActivity, ActivityPost::class.java).apply {
                    putExtra("postID", item.id)
                    putExtra("userID", item.userID)
                })
            }
        }
    }

    // 아이템 가져오기
    private fun initItemList() {
        swipeHome.isRefreshing = true // 로딩 시 인디케이터 보이기
        isLastPage = false

        val filteredQuery = firebaseDB.collection("board").let {
            var baseQuery: Query = it // 기본 쿼리 선언

            // 판매 여부 필터링
            when {
                forSale && !soldOut -> baseQuery = baseQuery.whereEqualTo("isSoldOut", false)
                !forSale && soldOut -> baseQuery = baseQuery.whereEqualTo("isSoldOut", true)
            }

            // 검색 유무에 따라서 분리
            if (searchQuery.isNullOrEmpty()) {
                baseQuery.orderBy("date", Query.Direction.DESCENDING)// 날짜 기준 내림차순
            } else {
                baseQuery
                    .orderBy("title")
                    .startAt(searchQuery)
                    .endAt(searchQuery + '\uf8ff')
            }
        }.limit(5) // 5개씩 끊어서 가져오기 (페이징)

        filteredQuery.get().addOnSuccessListener { documents ->
            val itemList = documents.map { document ->
                DataHome(
                    id = document.id,
                    title = document.getString("title"),
                    content = document.getString("content"),
                    price = document.getLong("price")?.toInt(),
                    date = document.getTimestamp("date"),
                    userID = document.getString("userID"),
                    image = document.getString("image"),
                    isSoldOut = document.getBoolean("isSoldOut")
                )
            }

            homeAdapter.itemList.clear()
            homeAdapter.itemList.addAll(itemList)
            homeAdapter.notifyDataSetChanged()
            if (documents.size() > 0) lastItem = documents.documents[documents.size() - 1]
            if (itemList.isEmpty()) txtNoProduct.visibility = View.VISIBLE else txtNoProduct.visibility - View.GONE
            swipeHome.isRefreshing = false
        }.addOnFailureListener {
            Toast.makeText(mainActivity, getString(R.string.str_home_get_board_failed), Toast.LENGTH_SHORT).show()
        }
    }

    // 다음 아이템 가져오기
    private fun loadNextItemList() {
        if (isLoading) return
        isLoading = true
        homeAdapter.setLoading(true)

        lastItem?.let { documentSnapshot ->
            val filteredQuery = firebaseDB.collection("board").let {
                var baseQuery: Query = it

                when {
                    forSale && !soldOut -> baseQuery = baseQuery.whereEqualTo("isSoldOut", false)
                    !forSale && soldOut -> baseQuery = baseQuery.whereEqualTo("isSoldOut", true)
                }

                if (searchQuery.isNullOrEmpty()) {
                    baseQuery.orderBy("date", Query.Direction.DESCENDING)
                        .startAfter(documentSnapshot)
                } else {
                    baseQuery.orderBy("title")
                        .startAt(searchQuery)
                        .endAt(searchQuery + '\uf8ff')
                        .endAt(searchQuery + '\uf8ff')
                        .startAfter(documentSnapshot)
                }
            }.limit(5)

            filteredQuery.get().addOnSuccessListener { documents ->
                val startPosition = homeAdapter.itemList.size
                val itemList = documents.map { document ->
                    DataHome(
                        id = document.id,
                        title = document.getString("title"),
                        content = document.getString("content"),
                        price = document.getLong("price")?.toInt(),
                        date = document.getTimestamp("date"),
                        userID = document.getString("userID"),
                        image = document.getString("image"),
                        isSoldOut = document.getBoolean("isSoldOut")
                    )
                }
                homeAdapter.itemList.addAll(itemList)
                homeAdapter.notifyItemRangeInserted(startPosition, itemList.size)
                if (documents.size() > 0) lastItem = documents.documents[documents.size() - 1]
                if (documents.size() < 5) isLastPage = true
                isLoading = false
                homeAdapter.setLoading(false)
            }.addOnFailureListener {
                Toast.makeText(mainActivity, getString(R.string.str_home_get_board_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}