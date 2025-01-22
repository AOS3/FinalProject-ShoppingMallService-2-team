package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindIdViewModel @Inject constructor(
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _userId = MutableLiveData<Pair<String, String>?>()
    val userId: LiveData<Pair<String, String>?> = _userId

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun findId(name: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val memberId = memberRepository.findMemberIdByNameAndPhone(name, phoneNumber)
                if (memberId != null) {
                    _userId.value = Pair(name, memberId)
                } else {
                    _errorMessage.value = "입력한 정보와 일치하는 아이디가 없습니다."
                }
            } catch (e: Exception) {
                _errorMessage.value = "오류가 발생했습니다. 다시 시도해주세요."
            }
        }
    }
}
