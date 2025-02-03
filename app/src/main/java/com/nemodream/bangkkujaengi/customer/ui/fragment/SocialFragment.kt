package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.nemodream.bangkkujaengi.customer.data.model.SocialCategoryType
import com.nemodream.bangkkujaengi.databinding.FragmentSocialBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.nemodream.bangkkujaengi.R

class SocialFragment: Fragment() {
    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    private var socialCategoryType: SocialCategoryType? = null
    private var currentPosition: Int = 0  // 현재 탭의 포지션을 저장


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 전달받은 socialCategoryType을 초기화
        socialCategoryType = SocialCategoryType.valueOf(
            arguments?.getString(KEY_SOCIAL_CATEGORY_TYPE) ?: SocialCategoryType.DISCOVERY.name
        )

        // 이전에 저장된 탭 상태가 있다면 상태값 가져오기
        savedInstanceState?.let {
            currentPosition = it.getInt(KEY_CURRENT_POSITION, 0)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupTabs()
        setupViewPager()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    * 리스너 모음 함수
    */
    private fun setupListeners() {
        with(binding) {
            toolbarSocial.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            fabCreatePost.setOnClickListener {
                findNavController().navigate(R.id.action_socialFragment_to_socialWritePictureFragment)
            }
        }
    }

    /*
    * 탭 설정
    */
    private fun setupTabs() {
        // SocialCategoryType의 항목을 탭으로 추가
        SocialCategoryType.entries.forEach { type ->
            binding.tabSocialCategory.addTab(binding.tabSocialCategory.newTab().setText(type.getSocialTabTitle()))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            delay(DELAY_TIME) // 초기 탭 선택 시 자연스러운 애니메이션을 위해 딜레이를 준다.
            // 뷰가 파괴되지 않았는지 확인
            if (_binding != null) {
                binding.tabSocialCategory.selectTab(binding.tabSocialCategory.getTabAt(currentPosition), true)
            }
        }
    }

    /*
    * 뷰 페이저 설정
    */
    private fun setupViewPager() {
        val adapter = SocialPagerAdapter(this)
        binding.viewPagerSocialCategory.adapter = adapter

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabSocialCategory, binding.viewPagerSocialCategory) { tab, position ->
            tab.text = SocialCategoryType.entries[position].getSocialTabTitle()
        }.attach()

        // 탭 페이지가 바뀔 때마다 currentPosition을 업데이트
        binding.viewPagerSocialCategory.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
            }
        })
    }


    companion object {
        private const val KEY_SOCIAL_CATEGORY_TYPE = "social_category_type"
        private const val KEY_CURRENT_POSITION = "current_position"
        private const val DELAY_TIME = 100L

        fun newInstance(type: SocialCategoryType): SocialFragment {
            return SocialFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_SOCIAL_CATEGORY_TYPE, type.name)
                }
            }
        }
    }
}

// ViewPager2의 어댑터
class SocialPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = SocialCategoryType.entries.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            SocialCategoryType.DISCOVERY.ordinal -> SocialDiscoveryFragment()
            SocialCategoryType.RANK.ordinal -> SocialRankFragment()
            SocialCategoryType.FOLLOWING.ordinal -> SocialFollowingFragment()
            else -> {
                SocialMyFragment()
            }
        }
    }
}
