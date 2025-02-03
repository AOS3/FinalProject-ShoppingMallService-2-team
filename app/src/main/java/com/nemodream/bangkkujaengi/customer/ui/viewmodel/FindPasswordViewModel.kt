package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import com.nemodream.bangkkujaengi.customer.data.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindPasswordViewModel @Inject constructor(
    private val memberRepository: MemberRepository
) : ViewModel() {

    val idInput = MutableLiveData<String>()
    val phoneNumberInput = MutableLiveData<String>()
    val verificationCodeInput = MutableLiveData<String>()

    private val _isVerificationButtonEnabled = MutableLiveData(false)
    val isVerificationButtonEnabled: LiveData<Boolean> get() = _isVerificationButtonEnabled

    private val _isVerificationCheckButtonEnabled = MutableLiveData(false)
    val isVerificationCheckButtonEnabled: LiveData<Boolean> get() = _isVerificationCheckButtonEnabled

    private val _isVerified = MutableLiveData<Boolean>()
    val isVerified: LiveData<Boolean> = _isVerified

    private val _isVerificationCodeSent = MutableLiveData<Boolean>()
    val isVerificationCodeSent: LiveData<Boolean> = _isVerificationCodeSent

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _memberId = MutableLiveData<String?>()
    val memberId: LiveData<String?> = _memberId

    fun requestVerification(id: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                _isVerificationButtonEnabled.value = false
                val isValidId = memberRepository.getUserById(id).first != null
                val isValidPhone = memberRepository.validateMemberIdAndPhone(id, phoneNumber)

                when {
                    !isValidId -> {
                        _errorMessage.value = "존재하지 않는 회원입니다."
                        _isVerificationButtonEnabled.value = true
                    }
                    !isValidPhone -> {
                        _errorMessage.value = "회원 정보와 일치하지 않습니다."
                        _isVerificationButtonEnabled.value = true
                    }
                    else -> {
                        _memberId.value = id
                        _isVerificationCodeSent.value = true
                        // _successMessage.value = "인증번호가 발송되었습니다."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "오류가 발생했습니다. 다시 시도해주세요."
                _isVerificationButtonEnabled.value = true
            }
        }
    }

    fun updateButtonStates() {
        _isVerificationButtonEnabled.value = !idInput.value.isNullOrBlank() && !phoneNumberInput.value.isNullOrBlank()
    }

    fun updateVerificationCheckButtonState() {
        _isVerificationCheckButtonEnabled.value = !verificationCodeInput.value.isNullOrBlank()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun onVerificationSuccess() {
        _isVerified.value = true
        _successMessage.value = "인증에 성공했습니다."
    }

}
