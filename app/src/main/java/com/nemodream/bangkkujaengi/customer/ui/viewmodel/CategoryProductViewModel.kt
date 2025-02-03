package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SortType
import com.nemodream.bangkkujaengi.customer.data.model.SubCategoryType
import com.nemodream.bangkkujaengi.customer.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryProductViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {
    // 카테고리를 저장하는 LiveData
    private val _categoryType = MutableLiveData<CategoryType>()

    // 선택된 서브카테고리를 저장하는 LiveData
    private val _selectedSubCategory = MutableLiveData(SubCategoryType.ALL)  // 초기값 설정

    // 현재 정렬 타입 - 초기값을 PRICE_HIGH로 변경
    private var _currentSortType = MutableLiveData(SortType.PRICE_HIGH)

    // 상품 목록을 저장하는 LiveData
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    // 정렬 텍스트를 위한 LiveData
    private val _sortText = MutableLiveData<String>()
    val sortText: LiveData<String> = _sortText

    private val _productLoading = MutableLiveData(false)
    val productLoading: LiveData<Boolean> = _productLoading

    private var userId = ""

    init {
        updateSortText(SortType.PURCHASE) // 기본으로 구매 많은 순
    }

    fun setUserId(id: String) {
        userId = id
    }

    /*
    * 정렬 타입 업데이트
    * */
    fun updateSort(sortType: SortType) {
        _currentSortType.value = sortType
        updateSortText(sortType)
        fetchProducts()
    }

    /*
    * 정렬 선택된 메뉴로 칩 택스트 변경
    * */
    private fun updateSortText(sortType: SortType) {
        _sortText.value = sortType.toDisplayString()
    }

    /*
    * 선택된 상위 카테고리를 업데이트하고 해당 카테고리의 첫 번째 서브카테고리를 자동으로 선택
    * */
    fun updateCategory(type: CategoryType) {
        _categoryType.value = type
        if (type == CategoryType.ALL) {
            _selectedSubCategory.value = SubCategoryType.ALL
            fetchProducts()  // 여기서 한 번만 호출
            return
        }
        SubCategoryType.getSubCategories(type).firstOrNull()?.let {
            _selectedSubCategory.value = it  // updateSubCategory() 대신 직접 값만 변경
            fetchProducts()  // 여기서 한 번만 호출
        }
    }

    /*
    * 선택된 서브카테고리를 업데이트하고 해당하는 상품 목록을 가져옴
    * */
    fun updateSubCategory(subCategory: SubCategoryType) {
        _selectedSubCategory.value = subCategory
        fetchProducts()
    }

    /*
    * 현재 선택된 카테고리와 서브카테고리에 해당하는 상품 목록을 가져옴
    * 성공 시: 상품 목록 업데이트
    * 실패 시: 에러 로깅 후 빈 목록으로 설정
    * */
    private fun fetchProducts() = viewModelScope.launch {
        _productLoading.value = true
        _products.value = emptyList()
        runCatching {
            _categoryType.value?.let { category ->
                _selectedSubCategory.value?.let { subCategory ->
                    _currentSortType.value?.let { sortType ->
                        repository.getProducts(category, subCategory, sortType, userId)
                    }
                }
            }
        }.onSuccess { products ->
            products?.let {
                _products.value = it
            }
            _productLoading.value = false
        }.onFailure { throwable ->
            Log.e("CategoryProductViewModel", "상품 정보를 가져오지 못했습니다", throwable)
            _products.value = emptyList()
            _productLoading.value = false
        }
    }

    fun toggleFavorite(memberId: String, productId: String) = viewModelScope.launch {
        runCatching {
            repository.toggleProductLikeState(memberId, productId)
        }.onSuccess {
            // 프로모션 아이템 좋아요 상태 변경
            val currentItems = _products.value?.toMutableList() ?: mutableListOf()

            // 프로모션 아이템들을 순회하면서 해당 상품의 좋아요 상태 업데이트
            val updatedItems = currentItems.map { item ->
                if (item.productId == productId) {
                    item.copy(like = !item.like)
                } else {
                    item
                }
            }

            _products.value = updatedItems
        }.onFailure { e ->
            Log.e("CategoryProductViewModel", "좋아요 상태 변경 실패: ", e)
        }
    }

}