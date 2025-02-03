package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseItem
import com.nemodream.bangkkujaengi.customer.data.repository.MyReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyReviewWriteListViewModel @Inject constructor(
    private val repository: MyReviewRepository
) : ViewModel() {

    private val _purchases = MutableLiveData<List<PurchaseItem>>()
    val purchases: LiveData<List<PurchaseItem>> = _purchases

    fun loadPurchases(documentId: String) {
        viewModelScope.launch {
            // Member ID 가져오기
            val memberId = repository.fetchMemberId(documentId) ?: return@launch

            // reviewState가 없는 상품 가져오기
            val purchases = repository.fetchPurchasesForWrite(memberId)
            _purchases.postValue(purchases)
        }
    }
}


