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
class ResetPasswordViewModel @Inject constructor(
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _isButtonEnabled = MutableLiveData<Boolean>()
    val isButtonEnabled: LiveData<Boolean> = _isButtonEnabled

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _isPasswordChanged = MutableLiveData<Boolean>()
    val isPasswordChanged: LiveData<Boolean> = _isPasswordChanged

    fun validateFields(password: String, confirmPassword: String) {
        _isButtonEnabled.value = password.isNotEmpty() && confirmPassword.isNotEmpty()
    }

    fun changePassword(memberId: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val isUpdated = memberRepository.updatePassword(memberId, newPassword)
                if (isUpdated) {
                    _successMessage.value = "비밀번호가 성공적으로 변경되었습니다."
                    _isPasswordChanged.value = true
                } else {
                    _errorMessage.value = "비밀번호 변경에 실패했습니다."
                }
            } catch (e: Exception) {
                _errorMessage.value = "오류가 발생했습니다: ${e.message}"
            }
        }
    }
}
