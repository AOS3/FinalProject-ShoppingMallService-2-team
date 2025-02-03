package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SortType
import com.nemodream.bangkkujaengi.customer.data.repository.PromotionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromotionViewModel @Inject constructor(
    private val promotionRepository: PromotionRepository,
): ViewModel() {
    private val _promotion = MutableLiveData<List<Product>>()
    val promotion: LiveData<List<Product>> = _promotion

    private val _sortText = MutableLiveData<String>()
    val sortText: LiveData<String> = _sortText

    private var currentTitle: String = ""
    private var currentSortType: SortType = SortType.LATEST

    private val _productLoading = MutableLiveData(false)
    val productLoading: LiveData<Boolean> = _productLoading

    private var userId: String = ""

    fun setUserId(id: String) {
        userId = id
    }

    fun getPromotionByTitle(title: String) {
        currentTitle = title
        // 프로모션 타입에 따른 기본 정렬
        val initialSortType = when {
            title.contains("인기") -> SortType.PURCHASE
            title.contains("할인") -> SortType.DISCOUNT
            else -> SortType.VIEWS
        }
        currentSortType = initialSortType
        updateSortText(initialSortType)
        getPromotionProducts(title, initialSortType)
    }

    fun updateSort(sortType: SortType) {
        currentSortType = sortType
        updateSortText(sortType)
        getPromotionProducts(currentTitle, sortType)
    }

    private fun updateSortText(sortType: SortType) {
        _sortText.value = sortType.toDisplayString()
    }

    private fun getPromotionProducts(title: String, sortType: SortType) = viewModelScope.launch {
        _productLoading.value = true
        runCatching {
            promotionRepository.getPromotionByTitle(title, sortType, userId= userId)
        }.onSuccess {
            _promotion.value = it
            _productLoading.value = false
        }.onFailure {
            it.printStackTrace()
            _productLoading.value = false
        }
    }

    fun toggleFavorite(memberId: String, productId: String) = viewModelScope.launch {
        runCatching {
            promotionRepository.toggleProductLikeState(memberId, productId)
            Log.d("HomeViewModel", "toggleFavorite: $productId")
        }.onSuccess {
            // 프로모션 아이템 좋아요 상태 변경
            val currentItems = _promotion.value?.toMutableList() ?: mutableListOf()
            // 프로모션 아이템들을 순회하면서 해당 상품의 좋아요 상태 업데이트
            val updatedItems = currentItems.map { item ->
                if (item.productId == productId) {
                    item.copy(like = !item.like)
                } else {
                    item
                }
            }

            _promotion.value = updatedItems
        }.onFailure { e ->
            Log.e("PromotionViewModel", "좋아요 상태 변경 실패: ", e)
        }
    }
}