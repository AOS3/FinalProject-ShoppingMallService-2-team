package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.repository.SearchRepository
import com.nemodream.bangkkujaengi.databinding.FragmentSearchBinding
import com.nemodream.bangkkujaengi.databinding.FragmentSocialWriteTagBottomSheetBinding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialWriteTagBottomSheetViewModel @Inject constructor(
    private val repository: SearchRepository,
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> get() = _searchResults

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 검색 결과 가져오기
    fun getProductsByProductName(productName: String, userId: String) = viewModelScope.launch {
        _isLoading.value = true
        runCatching {
            repository.getProductsByKeyword(productName, userId)
        }.onSuccess {
            _searchResults.value = it
            Log.d("SearchViewModel", "검색 결과: $it")
            _isLoading.value = false
        }.onFailure {
            it.printStackTrace()
            _isLoading.value = false
        }
    }
}