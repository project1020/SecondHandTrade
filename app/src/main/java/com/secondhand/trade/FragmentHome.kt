package com.secondhand.trade

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private lateinit var homeAdapter: AdapterHome
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

                searchView.queryHint = "물품 검색"
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {

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

        binding.swipeHome.setOnRefreshListener {
            homeAdapter.itemList.clear()
            initItemList()
        }

        initRecyclerview()
        initItemList()
        return binding.root
    }

    private fun initRecyclerview() {
        homeAdapter = AdapterHome(mainActivity)

        binding.recyclerHome.apply {
            adapter = homeAdapter
            addItemDecoration(RecyclerViewItemDecorator(5))
            setHasFixedSize(true)
            itemAnimator = null
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

    // 테스트용 아이템 리스트
    private fun initItemList() {
        binding.swipeHome.isRefreshing = true
        Handler(Looper.getMainLooper()).postDelayed({
            homeAdapter.itemList.apply {
                add(DataHome("https://firebasestorage.googleapis.com/v0/b/secondhandtrade-e2a57.appspot.com/o/image_product%2F20231103_123601.png?alt=media&token=a2a6340a-8539-4ee0-bec8-6fa06b3394d7&_gl=1*1tcikf9*_ga*NTQ0MTk3MDA2LjE2OTc3OTcyNTk.*_ga_CW55HF8NVT*MTY5ODk4NjUwNy4yNC4xLjE2OTg5ODY1NTguOS4wLjA.", "iPhone 15 Pro Max", "2023.10.04", 1550000, true))
                add(DataHome("https://firebasestorage.googleapis.com/v0/b/secondhandtrade-e2a57.appspot.com/o/image_product%2F20231103_123735.png?alt=media&token=8267161f-3f5c-4de7-ad73-4f0a3ec0c5df&_gl=1*u9yhnn*_ga*NTQ0MTk3MDA2LjE2OTc3OTcyNTk.*_ga_CW55HF8NVT*MTY5ODk4NjUwNy4yNC4xLjE2OTg5ODY2NDcuNjAuMC4w", "갤럭시 S23 Ultra", "2023.09.12", 1419000, false))
                add(DataHome("https://firebasestorage.googleapis.com/v0/b/secondhandtrade-e2a57.appspot.com/o/image_product%2F20231103_123812.png?alt=media&token=6c6f633e-1ade-4d82-b5c8-88f923930e61&_gl=1*1d7prr7*_ga*NTQ0MTk3MDA2LjE2OTc3OTcyNTk.*_ga_CW55HF8NVT*MTY5ODk4NjUwNy4yNC4xLjE2OTg5ODY3NDUuNjAuMC4w", "RTX 4090", "2022.10.12", 2700000, false))
                add(DataHome("https://firebasestorage.googleapis.com/v0/b/secondhandtrade-e2a57.appspot.com/o/image_product%2F20231103_123842.png?alt=media&token=a24eebbd-1b1a-4639-97a7-fdb54eadf097&_gl=1*6nl539*_ga*NTQ0MTk3MDA2LjE2OTc3OTcyNTk.*_ga_CW55HF8NVT*MTY5ODk4NjUwNy4yNC4xLjE2OTg5ODY4MDUuNjAuMC4w", "LG 그램 Style", "2023.01.24", 2300000, true))
                add(DataHome("https://firebasestorage.googleapis.com/v0/b/secondhandtrade-e2a57.appspot.com/o/image_product%2F20231105_110921.jpg?alt=media&token=8e6e93d0-645c-46d5-b022-c22dca33063a&_gl=1*1npxn2e*_ga*NTQ0MTk3MDA2LjE2OTc3OTcyNTk.*_ga_CW55HF8NVT*MTY5OTE1MDE4OC4yNi4xLjE2OTkxNTAyMzMuMTUuMC4w", "Topping L30 II + Topping E30 II", "2023.11.05", 300000, true))
            }
            homeAdapter.notifyDataSetChanged()
            binding.swipeHome.isRefreshing = false
        }, 2000)
    }
}