package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nemodream.bangkkujaengi.customer.data.paging.GalleryPagingSource
import com.nemodream.bangkkujaengi.customer.data.repository.ProfileEditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val galleryPagingSource: GalleryPagingSource,
    private val repository: ProfileEditRepository,
) : ViewModel() {

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData(false)
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _uploadedImageUrl = MutableLiveData<String>()
    val uploadedImageUrl: LiveData<String> = _uploadedImageUrl

    val galleryImages: Flow<PagingData<Uri>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false
        )
    ) {
        galleryPagingSource
    }.flow.cachedIn(viewModelScope)

    fun setSelectedImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun saveProfileImage(userId: String) = viewModelScope.launch {
        _isLoading.value = true
        selectedImageUri.value?.let { uri ->
            try {
                val imageUrl = repository.saveProfileImage(userId, uri)
                _uploadedImageUrl.value = imageUrl
                _isSuccess.value = true
            } catch (e: Exception) {
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

}