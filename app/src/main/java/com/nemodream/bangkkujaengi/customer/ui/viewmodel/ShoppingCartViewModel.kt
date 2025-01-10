package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShoppingCartViewModel() : ViewModel() {
    // 총 상품 가격 요약 텍스트
    // 총 상품 가격
    var tv_shopping_cart_tot_price_text = MutableLiveData<String>("10,000,000 원")
    // 총 할인 가격
    var tv_shopping_cart_tot_sale_price_text = MutableLiveData<String>("- 5,000,000 원")
    // 총 배송비 가격
    var tv_shopping_cart_tot_delivery_cost_text = MutableLiveData<String>("+ 3,000 원")
    // 총 합 금액
    var tv_shopping_cart_tot_sum_price_text = MutableLiveData<String>("123,123,123 원")
}