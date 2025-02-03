package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentMyReviewBinding

class MyReviewFragment : Fragment() {

    private lateinit var binding: FragmentMyReviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewPager2 어댑터 연결
        val pagerAdapter = MyReviewPagerAdapter(this)
        binding.viewPagerMyReview.adapter = pagerAdapter

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayoutMyReview, binding.viewPagerMyReview) { tab, position ->
            tab.text = when (position) {
                0 -> "리뷰 작성하기"
                1 -> "작성한 리뷰"
                else -> throw IllegalArgumentException("Invalid position")
            }
        }.attach()
        setupToolbar()
    }

    private fun setupToolbar() {
        // 좌측 뒤로가기 버튼 클릭 이벤트
        binding.toolbarMyReview.setNavigationOnClickListener {
            findNavController().popBackStack()  // 이전 화면으로 돌아가기
        }

        // 우측 메뉴 아이템 클릭 이벤트
        binding.toolbarMyReview.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_cart -> {
                    val action = MyReviewFragmentDirections.actionNavigationMyReviewToNavigationCart()
                    findNavController().navigate(action)  // 장바구니 화면으로 이동
                    true
                }
                else -> false
            }
        }
    }
}

// **탭에 표시할 Fragment들을 관리하는 어댑터**
class MyReviewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2  // 탭 개수

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyReviewWriteListFragment()  // 리뷰 작성하기 탭
            1 -> MyReviewWrittenListFragment()  // 작성한 리뷰 탭
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
