package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import com.nemodream.bangkkujaengi.customer.ui.adapter.CouponAdapter
import com.nemodream.bangkkujaengi.customer.ui.adapter.CouponReceiveClickListener
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.MyCouponViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentMyCouponBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.getUserType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyCouponFragment: Fragment(), CouponReceiveClickListener {
    private var _binding: FragmentMyCouponBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyCouponViewModel by viewModels()
    private val couponAdapter: CouponAdapter by lazy { CouponAdapter(this) }

    private val args: MyCouponFragmentArgs by navArgs()
    private val memberId: String by lazy { args.memberId }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCouponBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
        
    override fun onCouponReceiveClick(coupon: Coupon) {
        viewModel.receiveCoupon(requireContext().getUserId(), coupon)
    }

    private fun setupUI() {
        viewModel.getCouponList(requireContext().getUserId())
        with(binding) {
            rvMyCouponList.adapter = couponAdapter
        }
    }

    private fun setupListeners() {
        with(binding) {
            toolbarMyCoupon.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            toolbarMyCoupon.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_cart -> {
                        val action = MyCouponFragmentDirections.actionNavigationMyCouponToNavigationCart()
                        findNavController().navigate(action)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.couponList.observe(viewLifecycleOwner) { couponList ->
            couponAdapter.submitList(couponList)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                shimmerMyCoupon.root.visibility = if (isLoading) View.VISIBLE else View.GONE
                rvMyCouponList.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }

}