package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentPaymentCompletedBinding

class PaymentCompletedFragment : Fragment() {

    lateinit var fragmentPaymentCompletedBinding: FragmentPaymentCompletedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentPaymentCompletedBinding = FragmentPaymentCompletedBinding.inflate(inflater, container, false)

        // 이미지 세팅
        setting_image()

        // 홈으로 이동 버튼
        setting_btn_payment_completed_home()

        // 구매 내역 버튼
//        setting_btn_payment_completed_order_history()

        return fragmentPaymentCompletedBinding.root
    }

    // 이미지 세팅
    fun setting_image() {
        fragmentPaymentCompletedBinding.ivPaymentCompleted.setImageResource(R.drawable.ic_symbol)
    }

    // 홈으로 이동 버튼
    fun setting_btn_payment_completed_home() {
        fragmentPaymentCompletedBinding.btnPaymentCompletedHome.setOnClickListener {
            val action = PaymentCompletedFragmentDirections.popUptoPaymentCompletedFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    // 구매 내역 버튼
//    fun setting_btn_payment_completed_order_history() {
//        fragmentPaymentCompletedBinding.btnPaymentCompletedOrderHistory.setOnClickListener {
//            val action = PaymentCompletedFragmentDirections.popUptoPaymentCompletedFragmentToNavigationOrderDetails()
//            findNavController().navigate(action)
//        }
//
//    }

}