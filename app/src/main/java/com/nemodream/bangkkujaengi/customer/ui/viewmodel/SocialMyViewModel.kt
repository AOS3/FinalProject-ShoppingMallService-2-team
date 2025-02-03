package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.data.repository.SocialDiscoveryRepository
import com.nemodream.bangkkujaengi.customer.data.repository.SocialMyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialMyViewModel @Inject constructor(
    private val repository: SocialMyRepository
) : ViewModel() {

    // 내 프로필 바
    private val _myProfile = MutableLiveData<Member?>()
    val myProfile: LiveData<Member?> get() = _myProfile

    // 게시글 목록
    private val _posts = MutableLiveData<List<Post>>(emptyList())
    val posts: LiveData<List<Post>> get() = _posts


    // 내 프로필 업데이트
    fun loadMyProfile(memberDocId: String) = viewModelScope.launch {
        runCatching {
            repository.getMyProfile(memberDocId)
        }.onSuccess {
            _myProfile.value = it
        }.onFailure {
            it.printStackTrace()
        }
    }

    // 게시글을 로드하는 함수
    fun loadMyWrittenPosts(memberDocId: String) {
        viewModelScope.launch {
            val posts = repository.getMyWittenPosts(memberDocId) // 데이터 로드
            _posts.value = posts.ifEmpty { emptyList() } // 빈 리스트일 경우 빈 리스트로 설정
        }
    }


    // 내가 쓴 글 게시글 목록 로드
    fun loadMySavedPosts() {
        viewModelScope.launch {
            val posts = repository.getMySavedPosts() // 데이터 로드
            _posts.value = posts.ifEmpty { emptyList() } // 빈 리스트일 경우 빈 리스트로 설정
        }
    }

    fun updateNickname(newNickname: String) {
        val currentProfile = _myProfile.value
        if (currentProfile != null) {
            _myProfile.value = currentProfile.copy(memberNickName = newNickname)
        }
    }

}