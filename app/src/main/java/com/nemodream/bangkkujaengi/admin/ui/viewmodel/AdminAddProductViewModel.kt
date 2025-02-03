package com.nemodream.bangkkujaengi.admin.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.admin.data.repository.AdminProductRepository
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SubCategoryType
import com.nemodream.bangkkujaengi.utils.toCommaString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminAddProductViewModel @Inject constructor(
    private val adminProductRepository: AdminProductRepository,
) : ViewModel() {
    /*
    * 이미지 URI 목록 상태
    * */
    private val _imageUris = MutableLiveData<List<Uri>>(emptyList())
    val imageUris: LiveData<List<Uri>> = _imageUris

    /*
    * 상품 등록 버튼 활성화 상태
    * */
    private val _isSubmitEnabled = MutableLiveData(false)
    val isSubmitEnabled: LiveData<Boolean> = _isSubmitEnabled

    /*
    * 할인 판매가 상태
    * */
    private val _discountPrice = MutableLiveData("")
    val discountPrice: LiveData<String> = _discountPrice

    /*
    * 선택된 카테고리 상태
    * - null이면 카테고리 미선택
    * */
    private val _category = MutableLiveData<CategoryType?>(null)
    val category: LiveData<CategoryType?> = _category

    private val _subCategory = MutableLiveData<SubCategoryType?>(null)

    // 저장 성공 여부
    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    /*
    * 색상 리스트
    * 비었으면 색상 미선택
    * */
    private val _colors = MutableLiveData<List<String>>(emptyList())
    val colors: LiveData<List<String>> = _colors

    /*
    * 카테고리 설정
    * - 드롭다운에서 선택된 카테고리를 저장
    * @param category 선택된 카테고리
    * */
    fun setCategory(category: CategoryType) {
        _category.value = category
    }

    fun setSubCategory(subCategoryType: SubCategoryType?) {
        _subCategory.value = subCategoryType
    }

    fun getCategories(): List<CategoryType> =
        CategoryType.entries.filter { it != CategoryType.ALL }

    fun getSubCategories(categoryType: CategoryType): List<SubCategoryType> =
        SubCategoryType.getSubCategories(categoryType)
            .filter { !it.title.contains("전체") }

    fun setColors(color: String) {
        val currentColors = _colors.value?.toMutableList() ?: mutableListOf()
        currentColors.add(color)
        _colors.value = currentColors
    }

    /*
    * 이미지 추가
    * - 현재 이미지 목록에 새 이미지들을 추가
    * - 최대 이미지 개수 제한 준수
    * @param newUris 추가할 이미지 URI 목록
    * */
    fun addImages(newUris: List<Uri>) {
        val currentUris = _imageUris.value?.toMutableList() ?: mutableListOf()
        val remainingSlots = MAX_IMAGE_COUNT - currentUris.size
        val urisToAdd = newUris.take(remainingSlots)
        currentUris.addAll(urisToAdd)
        _imageUris.value = currentUris
    }

    /*
    * 이미지 제거
    * - 특정 URI의 이미지를 목록에서 제거
    * @param uri 제거할 이미지의 URI
    * */
    fun removeImage(uri: Uri) {
        _imageUris.value = _imageUris.value?.filter { it != uri }
    }

    /*
    * 할인 판매가 계산
    * - 원가와 할인율을 기반으로 할인 판매가 계산
    * - 입력값 유효성 검증 포함
    * @param priceText 원가 문자열
    * @param discountRateText 할인율 문자열
    * */
    fun calculateDiscountPrice(priceText: String, discountRateText: String) {
        if (priceText.isBlank() || discountRateText.isBlank()) {
            _discountPrice.value = ""
            return
        }

        try {
            val price = priceText.toDouble()
            val discountRate = discountRateText.toDouble()

            if (!validateDiscountRate(discountRate)) {
                _discountPrice.value = ""
                return
            }

            val calculatedPrice = price * (1 - discountRate / PERCENTAGE_DENOMINATOR)
            _discountPrice.value = calculatedPrice.toInt().toCommaString()
        } catch (e: NumberFormatException) {
            _discountPrice.value = ""
        }
    }

    /*
    * 입력 필드 유효성 검증
    * - 모든 필수 입력 필드와 이미지 존재 여부 확인
    * - 검증 결과에 따라 등록 버튼 활성화 상태 변경
    * */
    fun validateFields(
        title: String,
        description: String,
        price: String,
        discountRate: String,
        count: String
    ) {
        // 이미지도 함께 검증
        val hasImages = _imageUris.value?.isNotEmpty() == true
        val hasCategory = _category.value != null
        val hasSubCategory = _subCategory.value != null
        val hasColors = _colors.value?.isNotEmpty() == true
        val isValid = hasImages &&
                hasCategory &&
                hasSubCategory &&
                title.isNotBlank() &&
                description.isNotBlank() &&
                price.isNotBlank() &&
                discountRate.isNotBlank() &&
                count.isNotBlank() &&
                hasColors

        _isSubmitEnabled.value = isValid
    }

    /*
    * 할인율 범위 검증
    * - 할인율이 최소값과 최대값 사이에 있는지 확인
    * */
    private fun validateDiscountRate(discountRate: Double) =
        discountRate in MIN_DISCOUNT_RATE..MAX_DISCOUNT_RATE


    fun createAndSaveProduct(
        title: String,
        description: String,
        price: String,
        discountRate: String,
        count: String
    ) = viewModelScope.launch {
        runCatching {
            Product(
                productName = title,
                description = description,
                category = _category.value ?: CategoryType.ALL,
                subCategory = _subCategory.value ?: SubCategoryType.ALL,
                price = price.toInt(),
                saleRate = discountRate.toInt(),
                saledPrice = _discountPrice.value?.replace(",", "")?.toIntOrNull() ?: 0,
                productCount = count.toInt(),
                images = _imageUris.value?.map { it.toString() } ?: emptyList(),
                colors = _colors.value ?: emptyList(),
                searchKeywords = title.split(" "),
                likeCount = 0,
                createdAt = System.currentTimeMillis(),
                delete = false,
                like = false,
                isBest = false,
                purchaseCount = 0,
                reviewCount = 0,
                viewCount = 0,
            )
        }.onSuccess { product ->
            adminProductRepository.addProduct(product, _imageUris.value ?: emptyList())
            _isSuccess.value = true
        }.onFailure { e ->
            _isSuccess.value = false
        }
    }

    companion object {
        private const val MIN_DISCOUNT_RATE = 0.0
        private const val MAX_DISCOUNT_RATE = 95.0
        private const val MAX_IMAGE_COUNT = 5
        private const val PERCENTAGE_DENOMINATOR = 100
    }
}