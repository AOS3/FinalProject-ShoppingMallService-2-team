package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nemodream.bangkkujaengi.customer.deliverytracking.TrackingResponse

class OrderHistoryProductAdapterViewModel() : ViewModel() {

    // 배송 상태 리스트
    var shipping_status_list = MutableLiveData<TrackingResponse>()
}