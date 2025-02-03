package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val memberRepository: MemberRepository
) : ViewModel() {

    // 로그인 결과 LiveData
    private val _loginResult = MutableLiveData<Triple<Boolean, String, String?>>()
    val loginResult: LiveData<Triple<Boolean, String, String?>> = _loginResult

    fun signIn(id: String, password: String) {
        viewModelScope.launch {
            try {
                val (member, documentId) = memberRepository.getUserById(id)
                if (member == null) {
                    // 아이디가 존재하지 않는 경우
                    _loginResult.value = Triple(false, "존재하지 않는 아이디입니다.", null)
                } else if (member.memberPassword == password) {
                    // 로그인 성공
                    _loginResult.value = Triple(true, "로그인 성공", documentId)
                } else {
                    // 비밀번호가 틀린 경우
                    _loginResult.value = Triple(false, "잘못된 비밀번호입니다.", null)
                }
            } catch (e: Exception) {
                _loginResult.value = Triple(false, "로그인 처리 중 오류가 발생했습니다.", null)
            }
        }
    }

    fun signInAnonymously() = viewModelScope.launch {
        runCatching {
            memberRepository.signInAnonymously()
        }.onSuccess { uid ->
            if (uid != null) {
                _loginResult.value = Triple(true, "guest", uid)
            } else {
                _loginResult.value = Triple(false, "게스트 로그인에 실패했습니다.", null)
            }
        }.onFailure {
            _loginResult.value = Triple(false, "게스트 로그인에 실패했습니다.", null)
        }
    }
}