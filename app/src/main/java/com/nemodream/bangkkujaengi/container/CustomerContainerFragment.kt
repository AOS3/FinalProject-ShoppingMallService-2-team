package com.nemodream.bangkkujaengi.container

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.ui.fragment.HomeFragment
import com.nemodream.bangkkujaengi.customer.ui.fragment.SocialFragment
import com.nemodream.bangkkujaengi.databinding.FragmentCustomerContainerBinding
import com.nemodream.bangkkujaengi.utils.navigateToChildFragment

// ParentFragment.kt
class CustomerContainerFragment : Fragment() {
    private var _binding: FragmentCustomerContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigation()

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            navigateToChildFragment(HomeFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.customerBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateToChildFragment(HomeFragment())
                    true
                }
                R.id.navigation_social -> {
                    navigateToChildFragment(SocialFragment())
                    true
                }
                // 다른 메뉴 아이템들 추가 예정
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
