package com.nemodream.bangkkujaengi.customer.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nemodream.bangkkujaengi.customer.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductStateSharedViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _likeUpdate = MutableLiveData<Pair<String, Boolean>>()
    val likeUpdate: LiveData<Pair<String, Boolean>> = _likeUpdate

    fun updateLikeState(productId: String, isLiked: Boolean) {
        _likeUpdate.value = Pair(productId, isLiked)
    }
}