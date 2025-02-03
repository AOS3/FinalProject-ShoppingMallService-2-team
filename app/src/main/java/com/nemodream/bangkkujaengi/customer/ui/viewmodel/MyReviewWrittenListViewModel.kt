package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseItem
import com.nemodream.bangkkujaengi.customer.data.model.Review
import com.nemodream.bangkkujaengi.customer.data.repository.MyReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyReviewWrittenListViewModel @Inject constructor(
    private val repository: MyReviewRepository
) : ViewModel() {

    private val _writtenReviews = MutableLiveData<List<Review>>()
    val writtenReviews: LiveData<List<Review>> get() = _writtenReviews

    fun loadWrittenReviews(documentId: String) {
        viewModelScope.launch {
            // 회원 ID 가져오기
            val memberId = repository.fetchMemberId(documentId) ?: return@launch

            // Firestore에서 작성된 리뷰 데이터 가져오기
            val reviews = repository.fetchWrittenReviews(memberId)
            _writtenReviews.postValue(reviews)
        }
    }
}
