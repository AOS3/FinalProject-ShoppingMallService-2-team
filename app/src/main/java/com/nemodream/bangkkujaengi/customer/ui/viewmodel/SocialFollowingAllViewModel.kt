package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.repository.SocialFollowingAllRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialFollowingAllViewModel @Inject constructor(
    private val repository: SocialFollowingAllRepository
) : ViewModel() {

    // 팔로잉 멤버 목록 데이터를 관리
    private val _followingMembers = MutableLiveData<List<Member>>()
    val followingMembers: LiveData<List<Member>> get() = _followingMembers


    // 내 팔로잉 목록 업데이트
    fun loadFollowingAllMembers(memberDocId: String) = viewModelScope.launch {
        runCatching {
            repository.getFollowingMembers(memberDocId)
        }.onSuccess {
            _followingMembers.value = it

        }.onFailure {
            it.printStackTrace()
        }
    }
}