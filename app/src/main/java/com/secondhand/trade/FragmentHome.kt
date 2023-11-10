package com.secondhand.trade

import android.content.Context
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
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.secondhand.trade.databinding.FragmentHomeBinding

class FragmentHome : Fragment() {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val fabHome by lazy { binding.fabHome }
    private lateinit var homeAdapter: AdapterHome
    private lateinit var mainActivity: ActivityMain
    private val firestore = FirebaseFirestore.getInstance()
    private var lastItem: DocumentSnapshot? = null
    private var isLoading = false
    private var isLastPage = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as ActivityMain
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // 액션바 추가
        mainActivity.setSupportActionBar(binding.toolbarHome)
        // 메뉴 추가 (검색, 필터)
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home_toolbar_item, menu)
                val searchItem = menu.findItem(R.id.menuSearch)
                val searchView = searchItem.actionView as SearchView

                searchView.queryHint = "물품 검색"
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        // 검색 기능 구현
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
                                  // 필터링 기능 구현
                             },
                             onCancel = {}
                         ).show(childFragmentManager, "DialogHome")
                     }
                 }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // 당겨서 새로고침
        binding.swipeHome.setOnRefreshListener {
            homeAdapter.itemList.clear()
            initItemList()
        }

        initRecyclerview()
        initItemList()
        return binding.root
    }

    // recyclerview 설정
    private fun initRecyclerview() {
        homeAdapter = AdapterHome(mainActivity, binding.recyclerHome)

        binding.recyclerHome.apply {
            adapter = homeAdapter
            addItemDecoration(RecyclerViewItemDecorator(5)) // 아이템 간격 설정
            setHasFixedSize(true) // 아이템 크기가 고정되어 있음을 명시
            itemAnimator = null // 아이템 변경 애니메이션 삭제
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    // FloatingActionButton 확장 및 축소
                    if (dy > 0 && fabHome.isExtended) { // 위로 스크롤
                        fabHome.shrink() // FAB 축소
                    } else if (dy < 0 && !fabHome.isExtended) { // 아래로 스크롤
                        fabHome.extend() // FAB 확장
                    }
                    
                    // 맨 아래로 스크롤 시 다음 목록 가져오기
                    val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                    val itemTotalCount = recyclerView.adapter!!.itemCount - 1
                    if (!recyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                        // 다음 목록 가져오기
                        if (!isLoading && !isLastPage) loadNextItem()
                    }
                }
            })

            homeAdapter.setOnItemClickListener { item, position ->
                Toast.makeText(mainActivity, "item : $item, position : $position", Toast.LENGTH_SHORT).show()
            }

        }
    }

    // 테스트용 아이템 리스트
    private fun initItemList() {
        binding.swipeHome.isRefreshing = true // 로딩 시 인디케이터 보이기
        isLastPage = false
        
        // firestore 데이터베이스에서 글 목록 가져오기
        // 날짜 기준 내림차순 정렬
        firestore.collection("board_test").orderBy("date", Query.Direction.DESCENDING).limit(5).get().addOnSuccessListener { documents ->
            val itemList = documents.map { document ->
                DataHome(
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
            binding.swipeHome.isRefreshing = false
        }.addOnFailureListener {

        }
    }

    private fun loadNextItem() {
        if (isLoading) return
        isLoading = true
        homeAdapter.setLoading(true)

        lastItem?.let {
            firestore.collection("board_test").orderBy("date", Query.Direction.DESCENDING).startAfter(it).limit(5).get().addOnSuccessListener { documents ->
                val startPosition = homeAdapter.itemList.size
                val itemList = documents.map { document ->
                    DataHome(
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

            }
        }
    }
}