package com.nemodream.bangkkujaengi.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nemodream.bangkkujaengi.customer.data.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class FindPasswordViewModel @Inject constructor(
    private val memberRepository: MemberRepository
) : ViewModel() {

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

    private var generatedCode: String? = null

    fun requestVerification(id: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val isValidId = memberRepository.getUserById(id).first != null
                val isValidPhone = memberRepository.validateMemberIdAndPhone(id, phoneNumber)

                if (!isValidId) {
                    _errorMessage.value = "존재하지 않는 회원입니다."
                    _isVerificationCodeSent.value = false
                } else if (!isValidPhone) {
                    _errorMessage.value = "회원 정보와 일치하지 않습니다."
                    _isVerificationCodeSent.value = false
                } else {
                    _memberId.value = id // memberId 설정
                    generatedCode = generateVerificationCode()
                    sendVerificationCodeToPhone(phoneNumber, generatedCode!!)
                    _isVerificationCodeSent.value = true
                    _errorMessage.value = null
                    _successMessage.value = "인증번호가 발송되었습니다."
                }
            } catch (e: Exception) {
                _errorMessage.value = "오류가 발생했습니다. 다시 시도해주세요."
                _isVerificationCodeSent.value = false
            }
        }
    }

    fun verifyCode(inputCode: String) {
        if (inputCode == generatedCode) {
            _isVerified.value = true
            _errorMessage.value = null
            _successMessage.value = "인증에 성공했습니다."
        } else {
            _isVerified.value = false
            _errorMessage.value = "인증번호가 올바르지 않습니다."
        }
    }
    private fun generateVerificationCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    private fun sendVerificationCodeToPhone(phoneNumber: String, code: String) {
        // 실제로 SMS API를 호출하거나 테스트용 메시지를 출력
        Log.d("FindPasswordViewModel", "인증번호 [$code]가 번호 [$phoneNumber]로 발송되었습니다.")
    }
}
