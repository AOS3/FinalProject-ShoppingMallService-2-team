package com.nemodream.bangkkujaengi.admin.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.admin.ui.viewmodel.AdminOrderViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentAdminOrderBinding

class AdminOrderFragment : Fragment() {

    private lateinit var binding: FragmentAdminOrderBinding
    private lateinit var pagerAdapter: OrderPagerAdapter
    protected val viewModel: AdminOrderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPagerAndTabs()
        navigateBack()
        settingToolbarMenuClickListener()
    }

    // 뒤로가기
    private fun navigateBack(){
        binding.toolbarAdminOrder.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // 툴바 메뉴 클릭 리스너 설정
    private fun settingToolbarMenuClickListener() {
        val toolbar = view?.findViewById<MaterialToolbar>(R.id.toolbar_admin_order)
        toolbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_refresh -> {
                    refreshData()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupViewPagerAndTabs() {
        // Adapter 연결
        pagerAdapter = OrderPagerAdapter(this)
        binding.viewPagerAdminOrder.adapter = pagerAdapter

        // ViewPager2의 사용자 입력 비활성화 -> 좌우 스와이프로 탭 이동 불가
        binding.viewPagerAdminOrder.isUserInputEnabled = false

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayoutAdminOrder, binding.viewPagerAdminOrder) { tab, position ->
            tab.text = when (position) {
                0 -> "결제 완료"
                1 -> "상품 준비"
                2 -> "배송"
                3 -> "구매 확정"
                4 -> "취소"
                else -> throw IllegalArgumentException("Invalid position")
            }
        }.attach()

        // 탭 선택 시 새로고침 호출
        binding.tabLayoutAdminOrder.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 탭이 선택되면 새로고침
                refreshData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                // 탭이 다시 선택되면 새로고침
                refreshData()
            }
        })
    }

    // 현재 선택된 탭의 Fragment에 데이터 새로고침 요청
    private fun refreshData() {
        val currentFragment = childFragmentManager.findFragmentByTag(
            "f${binding.viewPagerAdminOrder.currentItem}"
        ) as? BaseAdminOrderFragment

        currentFragment?.refreshOrders()
    }

}

// ViewPager2 어댑터
class OrderPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 5 // 탭 개수
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminOrderPaymentCompletedFragment()
            1 -> AdminOrderProductReadyFragment()
            2 -> AdminOrderShippingFragment()
            3 -> AdminOrderPurchaseConfirmedFragment()
            4 -> AdminOrderCanceledFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
