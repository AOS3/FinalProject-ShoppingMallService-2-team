package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.Banner
import com.nemodream.bangkkujaengi.customer.data.model.PromotionHeader
import com.nemodream.bangkkujaengi.customer.data.model.PromotionItem
import com.nemodream.bangkkujaengi.customer.data.model.PromotionProducts
import com.nemodream.bangkkujaengi.customer.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    // 배너 아이템
    private var _bannerItems = MutableLiveData<List<Banner>>(emptyList())
    val bannerItems: LiveData<List<Banner>> = _bannerItems

    private var _bannerLoading = MutableLiveData(true)
    val bannerLoading: LiveData<Boolean> = _bannerLoading

    // 프로모션 아이템
    private var _promotionItems = MutableLiveData<List<PromotionItem>>(emptyList())
    val promotionItems: LiveData<List<PromotionItem>> = _promotionItems

    private val _promotionLoading = MutableLiveData(true)
    val promotionLoading: LiveData<Boolean> = _promotionLoading

    private var memberId: String = ""  // 기본값 설정

    private var isInitialized = false


    init {
        loadBannerItems()
    }

    fun setMemberId(id: String) {
        if (!isInitialized) {
            memberId = id
            isInitialized = true
        }
    }

    // 홈 화면 배너 리스트 불러오기
    private fun loadBannerItems() = viewModelScope.launch {
        _bannerLoading.value = true
        runCatching {
            homeRepository.getBanners()
        }.onSuccess { items ->
            _bannerItems.value = items
            _bannerLoading.value = false
        }.onFailure { e ->
            Log.d("HomeViewModel", "loadBannerItems: $e")
        }
    }

    // 홈 화면 프로모션 데이터 불러오기
    fun loadPromotions() = viewModelScope.launch {
        _promotionLoading.value = true
        runCatching {
            _promotionItems.value = emptyList()
            homeRepository.getPromotionSections(memberId)
        }.onSuccess { items ->
            _promotionItems.value = items
            _promotionLoading.value = false
        }.onFailure { e ->
            Log.d("HomeViewModel", "loadPromotions: $e")
        }
    }

    fun toggleFavorite(memberId: String, productId: String) = viewModelScope.launch {
        runCatching {
            homeRepository.toggleProductLikeState(memberId, productId)
        }.onSuccess {
            // 프로모션 아이템 좋아요 상태 변경
            val currentItems = _promotionItems.value?.toMutableList() ?: mutableListOf()

            // 프로모션 아이템들을 순회하면서 해당 상품의 좋아요 상태 업데이트
            val updatedItems = currentItems.map { item ->
                when (item) {
                    is PromotionProducts -> {
                        val updatedProducts = item.products.map { product ->
                            if (product.productId == productId) {
                                // 좋아요 상태와 카운트 업데이트
                                product.copy(
                                    like = !product.like,
                                    likeCount = if (!product.like) product.likeCount + 1
                                    else (product.likeCount - 1).coerceAtLeast(0)
                                )
                            } else product
                        }
                        item.copy(products = updatedProducts)
                    }

                    else -> item
                }
            }

            _promotionItems.value = updatedItems
        }.onFailure { e ->
            Log.e("HomeViewModel", "좋아요 상태 변경 실패: ", e)
        }
    }

    fun updateProductLikeState(productId: String, liked: Boolean) {
        val currentItems = _promotionItems.value?.toMutableList() ?: mutableListOf()

        val updatedItems = currentItems.map { item ->
            when (item) {
                is PromotionProducts -> {
                    val updatedProducts = item.products.map { product ->
                        if (product.productId == productId) {
                            product.copy(like = liked)
                        } else product
                    }
                    item.copy(products = updatedProducts)
                }
                else -> item
            }
        }

        _promotionItems.value = updatedItems
    }
}