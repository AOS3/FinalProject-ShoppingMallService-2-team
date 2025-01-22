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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AdminEditProductViewModel @Inject constructor(
    private val adminProductRepository: AdminProductRepository,
) : ViewModel() {
    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> = _product

    private val _storageImages = MutableLiveData<List<String>>(emptyList())
    private val _newImageUris = MutableLiveData<List<Uri>>(emptyList())

    private val _displayImages = MutableLiveData<List<Uri>>()
    val displayImages: LiveData<List<Uri>> = _displayImages

    private val _colors = MutableLiveData<List<String>>(emptyList())
    val colors: LiveData<List<String>> = _colors

    private val _isSubmitEnabled = MutableLiveData(true)
    val isSubmitEnabled: LiveData<Boolean> = _isSubmitEnabled

    private val _discountPrice = MutableLiveData("")
    val discountPrice: LiveData<String> = _discountPrice

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _category = MutableLiveData<CategoryType?>(null)
    private val _subCategory = MutableLiveData<SubCategoryType?>(null)

    private data class ValidationFields(
        val title: String,
        val description: String,
        val price: String,
        val discountRate: String,
        val count: String
    )

    private val _lastValidationFields = MutableLiveData<ValidationFields>()

    fun loadProduct(productId: String) = viewModelScope.launch {
        runCatching {
            adminProductRepository.getProduct(productId)
        }.onSuccess { product ->
            _product.value = product
            initializeProductData(product)
        }.onFailure {
            it.printStackTrace()
        }
    }


    private fun initializeProductData(product: Product) = viewModelScope.launch {
        val imageUrls = product.images.map { path ->
            Uri.parse(adminProductRepository.getImageUrl(path))
        }
        _displayImages.value = imageUrls
        _storageImages.value = product.images

        _colors.value = product.colors
        _category.value = product.category
        _subCategory.value = product.subCategory
        calculateDiscountPrice(
            product.price.toString(),
            product.saleRate.toString()
        )
        validateCurrentFields()
    }


    fun addNewImages(newUris: List<Uri>) {
        val currentUris = _newImageUris.value?.toMutableList() ?: mutableListOf()
        val totalImagesCount = (_storageImages.value?.size ?: 0) + currentUris.size
        val remainingSlots = MAX_IMAGE_COUNT - totalImagesCount
        val urisToAdd = newUris.take(remainingSlots)
        currentUris.addAll(urisToAdd)
        _newImageUris.value = currentUris
        updateDisplayImages()
    }

    private fun updateDisplayImages() {
        val storageUris = _storageImages.value?.map { Uri.parse(it) } ?: emptyList()
        val newUris = _newImageUris.value ?: emptyList()
        _displayImages.value = storageUris + newUris
        validateCurrentFields()
    }

    fun removeImage(uri: Uri) {
        val uriString = uri.toString()
        if (_storageImages.value?.contains(uriString) == true) {
            _storageImages.value = _storageImages.value?.filter { it != uriString }
        } else {
            _newImageUris.value = _newImageUris.value?.filter { it != uri }
        }
        updateDisplayImages()
    }

    fun calculateDiscountPrice(priceText: String, discountRateText: String) {
        if (priceText.isBlank() || discountRateText.isBlank()) {
            _discountPrice.value = ""
            return
        }

        try {
            val price = priceText.toDouble()
            val discountRate = discountRateText.toDouble()

            if (discountRate !in MIN_DISCOUNT_RATE..MAX_DISCOUNT_RATE) {
                _discountPrice.value = ""
                return
            }

            val calculatedPrice = price * (1 - discountRate / PERCENTAGE_DENOMINATOR)
            _discountPrice.value = calculatedPrice.toInt().toCommaString()
        } catch (e: NumberFormatException) {
            _discountPrice.value = ""
        }
    }

    fun addColor(color: String) {
        val currentColors = _colors.value?.toMutableList() ?: mutableListOf()
        currentColors.add(color)
        _colors.value = currentColors
        validateCurrentFields()
    }

    fun setCategory(category: CategoryType) {
        _category.value = category
        validateCurrentFields()
    }

    fun setSubCategory(subCategory: SubCategoryType?) {
        _subCategory.value = subCategory
        validateCurrentFields()
    }

    fun getCategories(): List<CategoryType> =
        CategoryType.entries.filter { it != CategoryType.ALL }

    fun getSubCategories(categoryType: CategoryType): List<SubCategoryType> =
        SubCategoryType.getSubCategories(categoryType)
            .filter { !it.title.contains("전체") }

    fun updateProduct(
        title: String,
        description: String,
        price: String,
        discountRate: String,
        count: String
    ) = viewModelScope.launch {
        val currentProduct = _product.value ?: return@launch

        runCatching {
            val updatedProduct = createProductData(
                productId = currentProduct.productId,
                title = title,
                description = description,
                price = price,
                discountRate = discountRate,
                count = count
            )

            adminProductRepository.updateProduct(
                updatedProduct,
                _newImageUris.value ?: emptyList()
            )
        }.onSuccess { success ->
            _isSuccess.value = success
        }.onFailure {
            _isSuccess.value = false
        }
    }

    private fun validateCurrentFields() {
        val lastValidation = _lastValidationFields.value
        if (lastValidation != null) {
            validateFields(
                lastValidation.title,
                lastValidation.description,
                lastValidation.price,
                lastValidation.discountRate,
                lastValidation.count
            )
        }
    }

    fun validateFields(
        title: String,
        description: String,
        price: String,
        discountRate: String,
        count: String
    ) {
        _lastValidationFields.value = ValidationFields(
            title, description, price, discountRate, count
        )

        val hasImages = (_storageImages.value?.size ?: 0) + (_newImageUris.value?.size ?: 0) > 0
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

    private fun createProductData(
        productId: String,
        title: String,
        description: String,
        price: String,
        discountRate: String,
        count: String
    ): Product = Product(
        productId = productId,
        productName = title,
        description = description,
        category = _category.value ?: throw IllegalStateException("Category is required"),
        subCategory = _subCategory.value ?: throw IllegalStateException("SubCategory is required"),
        price = price.toInt(),
        saleRate = discountRate.toInt(),
        saledPrice = _discountPrice.value?.replace(",", "")?.toIntOrNull() ?: 0,
        productCount = count.toInt(),
        images = _storageImages.value ?: emptyList(),
        colors = _colors.value ?: emptyList(),
        searchKeywords = title.split(" ")
    )

    companion object {
        private const val MIN_DISCOUNT_RATE = 0.0
        private const val MAX_DISCOUNT_RATE = 95.0
        private const val MAX_IMAGE_COUNT = 5
        private const val PERCENTAGE_DENOMINATOR = 100.0
    }
}