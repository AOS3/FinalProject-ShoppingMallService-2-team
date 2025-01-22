package com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentFindInfoBinding

class FindInfoFragment : Fragment() {

    private var _binding: FragmentFindInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewPager2 어댑터 설정
        val fragments = listOf(
            FindIdFragment(),
            FindPasswordFragment()
        )
        val adapter = FindInfoPagerAdapter(this, fragments)
        binding.viewPagerFindInfo.adapter = adapter

        // TabLayoutMediator로 TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayoutFindInfo, binding.viewPagerFindInfo) { tab, position ->
            tab.text = when (position) {
                0 -> "아이디 찾기"
                1 -> "비밀번호 찾기"
                else -> null
            }
        }.attach()

        navigateBack()
    }

    // 뒤로가기
    private fun navigateBack(){
        binding.toolbarFindInfo.setNavigationOnClickListener {
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class FindInfoPagerAdapter(
    fragment: Fragment,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}