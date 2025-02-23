package com.nemodream.bangkkujaengi.admin.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentAdminHomeBinding

class AdminHomeFragment: Fragment() {
    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        with(binding) {
            btnManageProduct.root.setOnClickListener {
                val action = AdminHomeFragmentDirections.actionAdminHomeFragmentToAdminProductFragment()
                findNavController().navigate(action)
            }

            btnManageBroadcastProduct.root.setOnClickListener {}
            btnManageBanner.root.setOnClickListener {}
            btnManageCoupon.root.setOnClickListener {
                val action = AdminHomeFragmentDirections.actionAdminHomeFragmentToAdminCouponFragment()
                findNavController().navigate(action)
            }
            btnManageOrder.root.setOnClickListener {
                val action = AdminHomeFragmentDirections.actionAdminHomeFragmentToAdminOrderFragment()
                findNavController().navigate(action)
            }
        }
    }
}