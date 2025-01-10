package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ShoppingCartViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentShoppingCartBinding

class ShoppingCartFragment : Fragment() {

    lateinit var fragmentShoppingCartBinding: FragmentShoppingCartBinding
    val shoppingCartViewModel: ShoppingCartViewModel by viewModels()

    var productList = Array(30) {
        "상품 테스트 ${it}"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentShoppingCartBinding  = FragmentShoppingCartBinding.inflate(inflater, container, false)

        // 툴바 세팅 메소드 호출
        setting_toolbar()
        // 초기 총 상품 가격 요약 텍스트 세팅 메소드 호출
        setting_price_textview()
        // 버튼 기능 메소드 호출
        fn_button()

        return fragmentShoppingCartBinding.root
    }

    // 툴바 세팅 메소드
    fun setting_toolbar() {
        
    }

    // 초기 총 상품 가격 요약 텍스트 세팅 메소드
    fun setting_price_textview() {
        // 총 상품 가격
        shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.observe(viewLifecycleOwner){
            fragmentShoppingCartBinding.tvShoppingCartTotPrice.text = it
        }

        // 총 할인 가격
        shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.observe(viewLifecycleOwner){
            fragmentShoppingCartBinding.tvShoppingCartTotSalePrice.text = it
        }

        // 총 배송비 가격
        shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.observe(viewLifecycleOwner){
            fragmentShoppingCartBinding.tvShoppingCartTotDeliveryCost.text = it
        }

        // 총 합 금액
        shoppingCartViewModel.tv_shopping_cart_tot_price_text.observe(viewLifecycleOwner){
            fragmentShoppingCartBinding.tvShoppingCartTotSumPrice.text = it
        }
    }

    fun setting_recyclerview() {

    }

    // 버튼 기능 메소드
    fun fn_button() {
        
    }


//    inner class shopping_cart_recyclerview_adapter {
//
//        inner class
//    }

}