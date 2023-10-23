package com.secondhand.trade

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.secondhand.trade.databinding.FragmentHomeBinding

class FragmentHome : Fragment() {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val fabHome by lazy { binding.fabHome }
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
                         // 필터 dialog 화면 띄우기
                         /*
                              - 가격 조건
                                ㄴ RangeSlider 사용
                                   ㄴ 가격 범위 (0원 ~ 1억원)
                                ㄴ slider 아래 edittext로 수동 입력

                              - 판매된 상품 제외
                                ㄴ MaterialButtonToggleGroup 사용
                                ㄴ DB에서 판매 중, 판매 완료 구분
                         */
                     }
                 }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // recyclerview 스크롤 리스너
        binding.recyclerHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fabHome.isExtended) { // 위로 스크롤
                    fabHome.shrink() // FAB 축소
                } else if (dy < 0 && !fabHome.isExtended) { // 아래로 스크롤
                    fabHome.extend() // FAB 확장
                }
            }
        })
        return binding.root
    }
}
