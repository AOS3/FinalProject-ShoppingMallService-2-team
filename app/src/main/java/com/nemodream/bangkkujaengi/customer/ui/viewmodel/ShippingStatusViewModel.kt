package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import com.nemodream.bangkkujaengi.customer.deliverytracking.TrackingResponse

class ShippingStatusViewModel() : ViewModel() {

    // 구매 내역 항목 리스트
    var shipping_purchase_product = MutableLiveData<Purchase>()

    // 배송 상태 리스트
    var shipping_status_list = MutableLiveData<TrackingResponse>()

}