package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repository: MyPageRepository,
): ViewModel() {

    // 멤버 정보
    private val _memberInfo = MutableLiveData<Member>()
    val memberInfo: LiveData<Member> = _memberInfo

    // 최근 본 상품
    private val _recentProductList = MutableLiveData<List<Product>>(emptyList())
    val recentProductList: LiveData<List<Product>> = _recentProductList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _nicknameUpdateState = MutableLiveData<NicknameUpdateUiState>()
    val nicknameUpdateState: LiveData<NicknameUpdateUiState> = _nicknameUpdateState

    // 멤버 정보 가져오기
    fun getMemberInfo(memberDocId: String) = viewModelScope.launch {
        runCatching {
            repository.getMemberInfo(memberDocId)
        }.onSuccess {
            _memberInfo.value = it
        }.onFailure {
            it.printStackTrace()
        }
    }

    // 최근 본 상품을 불러오는 코드
    fun loadRecentProduct(userId: String) = viewModelScope.launch {
        runCatching {
            _isLoading.value = true
            val products = repository.loadRecentProduct(userId)
            products.map { product ->
                val imageUrl = repository.getImageUrl(product.images.first())
                product.copy(images = listOf(imageUrl))
            }
        }.onSuccess {
            _recentProductList.value = it
            _isLoading.value = false
        }.onFailure {
            it.printStackTrace()
            _isLoading.value = false
        }
    }

    fun toggleFavorite(memberId: String, productId: String) = viewModelScope.launch {
        runCatching {
            repository.toggleProductLikeState(memberId, productId)
            Log.d("HomeViewModel", "toggleFavorite: $productId")
        }.onSuccess {
            // 프로모션 아이템 좋아요 상태 변경
            val currentItems = _recentProductList.value?.toMutableList() ?: mutableListOf()
            // 프로모션 아이템들을 순회하면서 해당 상품의 좋아요 상태 업데이트
            val updatedItems = currentItems.map { item ->
                if (item.productId == productId) {
                    item.copy(like = !item.like)
                } else {
                    item
                }
            }

            _recentProductList.value = updatedItems
        }.onFailure { e ->
            Log.e("MyPageViewModel", "좋아요 상태 변경 실패: ", e)
        }
    }

    // 닉네임 수정
    fun updateNickname(userId: String, editedNickname: String) = viewModelScope.launch {
        repository.updateNickname(userId, editedNickname)
            .onSuccess {
                _memberInfo.value = _memberInfo.value?.copy(memberNickName = editedNickname)
                _nicknameUpdateState.value = NicknameUpdateUiState.Success()
            }
            .onFailure { exception ->
                _nicknameUpdateState.value = NicknameUpdateUiState.Error(
                    exception.message ?: "닉네임 변경에 실패했습니다."
                )
            }
    }

}

sealed class NicknameUpdateUiState {
    data class Success(val message: String = "닉네임이 변경되었습니다.") : NicknameUpdateUiState()
    data class Error(val message: String) : NicknameUpdateUiState()
}
