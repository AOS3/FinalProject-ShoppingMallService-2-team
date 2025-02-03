package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.data.repository.SocialFollowingRepository
import com.nemodream.bangkkujaengi.utils.getUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialFollowingViewModel @Inject constructor(
    private val repository: SocialFollowingRepository
) : ViewModel() {

    // 팔로잉 멤버 목록 데이터를 관리
    private val _followingMembers = MutableLiveData<List<Member>>()
    val followingMembers: LiveData<List<Member>> get() = _followingMembers

    // 선택한 멤버의 게시글 목록 데이터를 관리
    private val _memberPosts = MutableLiveData<List<Post>>()
    val memberPosts: LiveData<List<Post>> get() = _memberPosts

    // 현재 선택된 멤버를 관리
    private val _selectedMember = MutableLiveData<Member?>()
    val selectedMember: LiveData<Member?> get() = _selectedMember

    // 멤버별 팔로잉 상태를 관리
    private val followingStates = mutableMapOf<String, Boolean>()

    // 선택된 멤버의 팔로잉 상태
    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> get() = _isFollowing

    // Firebase Firestore와 FirebaseAuth 초기화
    private val firestore = FirebaseFirestore.getInstance()


    // 내 팔로잉 목록 업데이트
    fun loadFollowingMembers(memberDocId: String) = viewModelScope.launch {
        runCatching {
            repository.getFollowingMembers(memberDocId)
        }.onSuccess {
            _followingMembers.value = it

            // 각 멤버의 초기 팔로잉 상태를 true로 설정
            it.forEach { member ->
                followingStates[member.id] = true
            }

            // 첫 번째 멤버를 기본 선택
            if (it.isNotEmpty()) {
                selectMember(it.first())
            }

        }.onFailure {
            it.printStackTrace()
        }
    }

    // 특정 멤버를 선택
    fun selectMember(member: Member) {
        _selectedMember.value = member // 선택된 멤버 설정
        _isFollowing.value = followingStates[member.id] ?: false // 해당 멤버의 팔로잉 상태 설정
        loadMemberPosts(member) // 해당 멤버의 게시글 로드
    }

    // 선택된 멤버의 팔로잉 상태를 반전 및 업데이트
    fun toggleFollowing(currentUserId:String) {
        val selectedMember = _selectedMember.value ?: return
        val currentState = followingStates[selectedMember.id] ?: false
        val newState = !currentState

        // 팔로잉 상태 업데이트
        followingStates[selectedMember.id] = newState
        _isFollowing.value = newState

        // 현재 선택된 멤버의 인덱스를 찾기
        val currentIndex = _followingMembers.value?.indexOf(selectedMember) ?: -1

        // `followingList` 업데이트
        val updatedFollowingList = _followingMembers.value.orEmpty().toMutableList()
        if (newState) {
            updatedFollowingList.add(selectedMember)
        } else {
            updatedFollowingList.remove(selectedMember)
            removeFromFollowingList(currentUserId, selectedMember)
            updateMyFollowingCount(currentUserId)
            updateSelectedUserFollowerCount(selectedMember)
        }

        // 전체 팔로잉 멤버 목록 업데이트
        _followingMembers.value = updatedFollowingList

        // 삭제 후에도 선택 상태를 유지
        if (currentIndex >= 0 && currentIndex < updatedFollowingList.size) {
            selectMember(updatedFollowingList[currentIndex])
        } else if (currentIndex > 0) {
            // 마지막 멤버를 삭제한 경우 이전 멤버 선택
            selectMember(updatedFollowingList[currentIndex - 1])
        } else{
            // 모두 삭제된 경우 선택 해제
            _selectedMember.value = null
            _memberPosts.value = emptyList()
        }
    }


    // 팔로잉 멤버 게시글 업데이트
    fun loadMemberPosts(member: Member) = viewModelScope.launch {
        runCatching {
            repository.getPostsByMember(member)
        }.onSuccess {
            _memberPosts.value = it

        }.onFailure {
            it.printStackTrace()
        }
    }

    // 현재 로그인한 유저의 팔로잉 리스트에서 멤버 삭제
    private fun removeFromFollowingList(currentUserId: String, selectmember: Member) {

        val selectedMemberId = firestore.collection("Member").document(selectmember.id)

        Log.d("팔로잉 탭 Follow", "${currentUserId}, ${selectedMemberId}")
        // Firestore에서 로그인한 유저의 문서를 가져와서 followingList 업데이트
        firestore.collection("Member")
            .document(currentUserId)
            .update("followingList", FieldValue.arrayRemove(selectedMemberId)) // 팔로잉 리스트에서 해당 멤버를 삭제
            .addOnSuccessListener {
                Log.d("팔로잉 탭 Follow", "언팔로우 성공")
            }
            .addOnFailureListener { e ->
                Log.e("팔로잉 탭 Follow", "언팔로우 실패: ${e.message}")
            }
    }

    // 언팔로우 시 내 팔로잉 카운트 업데이트
    private fun updateMyFollowingCount(currentUserId: String) {
        // 나의 followingCount 감소
        firestore.collection("Member").document(currentUserId)
            .update("followingCount", FieldValue.increment(-1.toLong()))
    }

    // 언팔로우 시 선택 유저의 팔로워 카운트 업데이트
    private fun updateSelectedUserFollowerCount(selectmember: Member) {
        val selectedMemberId = firestore.collection("Member").document(selectmember.id)

        // 유저 A의 followerCount 감소
        firestore.collection("Member").document(selectmember.id)
            .update("followerCount", FieldValue.increment(-1.toLong()))
    }
}