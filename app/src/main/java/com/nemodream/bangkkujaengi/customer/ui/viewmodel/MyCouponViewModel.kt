package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import com.nemodream.bangkkujaengi.customer.data.repository.CouponRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyCouponViewModel @Inject constructor(
    private val couponRepository: CouponRepository,
): ViewModel() {

    // 쿠폰 리스트
    private val _couponList = MutableLiveData<List<Coupon>>(emptyList())
    val couponList: LiveData<List<Coupon>> = _couponList

    // 로딩
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading


    fun getCouponList(memberId: String) = viewModelScope.launch {
        _isLoading.value = true
        runCatching {
            couponRepository.getCouponList(memberId)
        }.onSuccess {
            _couponList.value = it
            _isLoading.value = false
            Log.d("getCouponList", "getCouponList: $it")
        }.onFailure {
            it.printStackTrace()
            _isLoading.value = false
        }
    }

    // 멤버에게 쿠폰 id를 저장한다.
    fun receiveCoupon(userId: String, coupon: Coupon) = viewModelScope.launch {
        runCatching {
            couponRepository.receiveCoupon(userId, coupon)
        }.onSuccess {
            // 성공 했다면 해당 쿠폰을 리스트에 다시 갱신한다.
            _couponList.value = _couponList.value?.map {
                if (it.id == coupon.id) {
                    it.copy(isHold = true)
                } else {
                    it
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }
}