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
import androidx.recyclerview.widget.RecyclerView
import com.secondhand.trade.databinding.FragmentHomeBinding

class FragmentHome : Fragment() {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val fabHome by lazy { binding.fabHome }
//    private lateinit var homeAdapter: AdapterHome
    private lateinit var mainActivity: ActivityMain
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

                searchView.queryHint = "게시글 검색"
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        // 검색 버튼을 눌렀을 때 이벤트 처리
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        // 검색어 입력 중에 이벤트 처리
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                 when (menuItem.itemId) {
                     R.id.menuFilter -> {
                         // 필터 다이얼로그 띄우기
                         DialogHome(
                             onApply = { minValue, maxValue, forSale, soldOut ->
                                 Toast.makeText(mainActivity, "min=$minValue, max=$maxValue, forSale=$forSale, soldOut=$soldOut", Toast.LENGTH_SHORT).show() // 반환 값 출력
                             },
                             onCancel = {}
                         ).show(childFragmentManager, "DialogHome")
                     }
                 }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        initRecyclerview()
        return binding.root
    }

    private fun initRecyclerview() {
//        homeAdapter = AdapterHome(mainActivity)
//        homeAdapter.itemList.apply {
//            add(DataHome(R.mipmap.ic_launcher, "제목1", "2023.10.04", 10000))
//            add(DataHome(R.mipmap.ic_launcher, "제목2", "2023.10.04", 100000))
//            add(DataHome(R.mipmap.ic_launcher, "제목3", "2023.10.03", 2000))
//            add(DataHome(R.mipmap.ic_launcher, "제목4", "2023.10.03", 30000))
//            add(DataHome(R.mipmap.ic_launcher, "제목5", "2023.10.03", 10000))
//            add(DataHome(R.mipmap.ic_launcher, "제목6", "2023.10.02", 50000))
//            add(DataHome(R.mipmap.ic_launcher, "제목7", "2023.10.02", 100))
//            add(DataHome(R.mipmap.ic_launcher, "제목8", "2023.10.02", 10000))
//        }
//        homeAdapter.notifyDataSetChanged()

        binding.recyclerHome.apply {
//            adapter = homeAdapter
            addItemDecoration(RecyclerViewItemDecorator(5))
            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && fabHome.isExtended) { // 위로 스크롤
                        fabHome.shrink() // FAB 축소
                    } else if (dy < 0 && !fabHome.isExtended) { // 아래로 스크롤
                        fabHome.extend() // FAB 확장
                    }
                }
            })
        }
    }
}
