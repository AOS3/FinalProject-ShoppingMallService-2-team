package com.nemodream.bangkkujaengi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SignUpViewModel
import com.nemodream.bangkkujaengi.databinding.ActivitySignUpBinding
import com.nemodream.bangkkujaengi.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels()

    // Firebase 인증과 관련된 변수
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FirebaseAuth 초기화
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navigateBack()
        observeViewModel()
        setupListeners()
    }

    // 뒤로가기
    private fun navigateBack() {
        binding.toolbarSignUp.setNavigationOnClickListener {
            finish()
        }
    }

    // ViewModel의 LiveData 관찰 및 에러 메시지 처리
    private fun observeViewModel() {
        signUpViewModel.nameError.observe(this) { errorMessage ->
            binding.tfSignUpName.isErrorEnabled = !errorMessage.isNullOrEmpty()
            binding.tfSignUpName.error = errorMessage
        }
        signUpViewModel.idError.observe(this) { errorMessage ->
            binding.tfSignUpId.isErrorEnabled = !errorMessage.isNullOrEmpty()
            binding.tfSignUpId.error = errorMessage
        }
        signUpViewModel.nicknameError.observe(this) { errorMessage ->
            binding.tfSignUpNickname.isErrorEnabled = !errorMessage.isNullOrEmpty()
            binding.tfSignUpNickname.error = errorMessage
        }
        signUpViewModel.passwordError.observe(this) { errorMessage ->
            binding.tfSignUpPw.isErrorEnabled = !errorMessage.isNullOrEmpty()
            binding.tfSignUpPw.error = errorMessage
        }
        signUpViewModel.chkPasswordError.observe(this) { errorMessage ->
            binding.tfSignUpPwCheck.isErrorEnabled = !errorMessage.isNullOrEmpty()
            binding.tfSignUpPwCheck.error = errorMessage
        }
        signUpViewModel.phoneNumberError.observe(this) { errorMessage ->
            binding.tfSignUpPhoneNumber.isErrorEnabled = !errorMessage.isNullOrEmpty()
            binding.tfSignUpPhoneNumber.error = errorMessage
        }
        signUpViewModel.verificationCodeError.observe(this) { errorMessage ->
            binding.tfSignUpVerificationCode.isErrorEnabled = !errorMessage.isNullOrEmpty()
            binding.tfSignUpVerificationCode.error = errorMessage
        }

        // 아이디 중복 확인 결과 관찰
        signUpViewModel.idResult.observe(this) { result ->
            val (isAvailable, message) = result
            Log.d("nickname", "아이디 사용 가능: $isAvailable, 메시지: $message")
            showToast(message)
        }

        signUpViewModel.nicknameResult.observe(this) { result ->
            val (isAvailable, message) = result
            Log.d("nickname", "닉네임 사용 가능: $isAvailable, 메시지: $message")
            showToast(message)
        }

        // 아이디 중복 확인 버튼 상태 관찰
        signUpViewModel.isIdCheckButtonEnabled.observe(this) { isEnabled ->
            binding.btnSignUpCheckId.isEnabled = isEnabled
            // binding.btnSignUpCheckId.alpha = if (isEnabled) 1f else 0.5f
        }

        // 닉네임 중복 확인 버튼 상태 관찰
        signUpViewModel.isNicknameCheckButtonEnabled.observe(this) { isEnabled ->
            binding.btnSignUpCheckNickname.isEnabled = isEnabled
            // binding.btnSignUpCheckNickname.alpha = if (isEnabled) 1f else 0.5f
        }

        // 가입 버튼 상태 관찰
        signUpViewModel.isSignUpButtonEnabled.observe(this) { isEnabled ->
            binding.btnSignUpSignup.isEnabled = isEnabled
            // binding.btnSignUpSignup.alpha = if (isEnabled) 1f else 0.5f
        }

        signUpViewModel.isVerificationButtonEnabled.observe(this) { isEnabled ->
            binding.btnSignUpVerification.isEnabled = isEnabled
            // binding.btnSignUpVerification.alpha = if (isEnabled) 1f else 0.5f
        }

        signUpViewModel.isVerificationCheckButtonEnabled.observe(this) { isEnabled ->
            binding.btnSignUpCheckVerification.isEnabled = isEnabled
            // binding.btnSignUpCheckVerification.alpha = if (isEnabled) 1f else 0.5f
        }
    }

    // 입력 리스너
    private fun setupListeners() {
        // 빈 공간 터치 시 키보드 숨김 처리
        binding.root.setOnClickListener {
            binding.root.hideKeyboard()
            binding.root.clearFocus() // 포커스 제거
        }

        binding.tfSignUpName.editText?.addTextChangedListener { editable ->
            signUpViewModel.name.value = editable.toString()
            signUpViewModel.validateName()
            signUpViewModel.validateSignUpButton()
        }
        binding.tfSignUpId.editText?.addTextChangedListener { editable ->
            signUpViewModel.id.value = editable.toString()
            signUpViewModel.validateId()
            signUpViewModel.validateIdCheckButton()
            signUpViewModel.validateSignUpButton()
        }
        binding.tfSignUpNickname.editText?.addTextChangedListener { editable ->
            signUpViewModel.nickname.value = editable.toString()
            signUpViewModel.validateNickname()
            signUpViewModel.validateNicknameCheckButton()
            signUpViewModel.validateSignUpButton()
        }
        binding.tfSignUpPw.editText?.addTextChangedListener { editable ->
            signUpViewModel.password.value = editable.toString()
            signUpViewModel.validatePassword()
            signUpViewModel.validateSignUpButton()
        }
        binding.tfSignUpPwCheck.editText?.addTextChangedListener { editable ->
            signUpViewModel.chkPassword.value = editable.toString()
            signUpViewModel.validateChkPassword()
            signUpViewModel.validateSignUpButton()
        }
        binding.tfSignUpPhoneNumber.editText?.addTextChangedListener { editable ->
            signUpViewModel.phoneNumber.value = editable.toString()
            signUpViewModel.validatePhoneNumber()
            signUpViewModel.validateVerificationButton() // 인증 요청 버튼 활성화 상태 업데이트
            signUpViewModel.validateSignUpButton()
        }
        binding.tfSignUpVerificationCode.editText?.addTextChangedListener { editable ->
            signUpViewModel.verificationCode.value = editable.toString()
            signUpViewModel.validateVerificationCode()
            signUpViewModel.validateVerificationCheckButton() // 인증 확인 버튼 활성화 상태 업데이트
            signUpViewModel.validateSignUpButton()
        }
        // 아이디 중복 확인 버튼 클릭 이벤트
        binding.btnSignUpCheckId.setOnClickListener {
            val inputId = binding.tfSignUpId.editText?.text.toString()
            signUpViewModel.checkIdAvailability(inputId)
            binding.btnSignUpCheckId.isEnabled = false
        }

        // 닉네임 중복 확인 버튼 클릭 이벤트
        binding.btnSignUpCheckNickname.setOnClickListener {
            val inputNickname = binding.tfSignUpNickname.editText?.text.toString()
            signUpViewModel.checkNicknameAvailability(inputNickname)
            binding.btnSignUpCheckNickname.isEnabled = false
        }

        // 가입하기 버튼 클릭 이벤트
        binding.btnSignUpSignup.setOnClickListener {
            binding.btnSignUpSignup.isEnabled = false
            val hasErrors = listOf(
                signUpViewModel.nameError.value,
                signUpViewModel.idError.value,
                signUpViewModel.nicknameError.value,
                signUpViewModel.passwordError.value,
                signUpViewModel.chkPasswordError.value,
                signUpViewModel.phoneNumberError.value,
                signUpViewModel.verificationCodeError.value
            ).any { !it.isNullOrEmpty() }

            if (hasErrors) {
                showToast("입력한 내용을 확인해주세요.")
                binding.btnSignUpSignup.isEnabled = true
            } else {
                signUpViewModel.registerMember { success, documentId ->
                    if (success) {
                        showToast("회원가입 완료! 문서 ID: $documentId")
                        finish()
                    } else {
                        showToast("회원가입 실패")
                        binding.btnSignUpSignup.isEnabled = true
                    }
                }
            }
        }

        // 전화번호 입력 리스너
        binding.tfSignUpPhoneNumber.editText?.addTextChangedListener { editable ->
            signUpViewModel.phoneNumber.value = editable.toString()
            signUpViewModel.validatePhoneNumber()
        }

        // 인증 코드 전송 버튼
        binding.btnSignUpVerification.setOnClickListener {
            val rawPhoneNumber = binding.tfSignUpPhoneNumber.editText?.text.toString()
            val formattedPhoneNumber = formatPhoneNumberToE164(rawPhoneNumber)
            if (isValidPhoneNumber(formattedPhoneNumber)) {
                startPhoneNumberVerification(formattedPhoneNumber)
                binding.btnSignUpVerification.isEnabled = false
                binding.btnSignUpVerification.postDelayed({
                    binding.btnSignUpVerification.isEnabled = true
                }, 60000) // 60초 후 버튼 활성화
            } else {
                showToast("전화번호를 올바르게 입력하세요. 예: 01012345678")
            }
        }

        // 인증 코드 확인 버튼
        binding.btnSignUpCheckVerification.setOnClickListener {
            val code = binding.tfSignUpVerificationCode.editText?.text.toString()
            if (code.isNotEmpty() && storedVerificationId != null) {
                verifyCode(code)
                binding.btnSignUpCheckVerification.isEnabled = false
            } else {
                showToast("인증 코드를 입력하거나 인증 요청을 다시 진행하세요.")
            }
        }
    }

    // Firebase 전화번호 인증 요청
    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // 입력된 실제 번호로 인증번호 전송
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this) // 현재 액티비티
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    showToast("인증 실패: ${e.message}")
                    Log.w("PhoneAuth", "Verification failed", e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    showToast("인증 코드가 전송되었습니다.")
                    Log.d("PhoneAuth", "Code sent: $verificationId")
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 인증 코드 확인
    private fun verifyCode(code: String) {
        if (storedVerificationId != null) {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            showToast("인증 요청을 먼저 진행해주세요.")
        }
    }

    // Firebase 인증 성공 처리
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("전화번호 인증 성공!")
                    signUpViewModel.verifyCodeSuccess() // 인증 성공 시 가입 버튼 활성화
                    Log.d("PhoneAuth", "Sign in successful")
                } else {
                    showToast("전화번호 인증 실패.")
                    Log.w("PhoneAuth", "Sign in failed", task.exception)
                }
            }
    }

    // 전화번호 형식 검증
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneRegex = Regex("^\\+[1-9]\\d{1,14}$")
        return phoneRegex.matches(phoneNumber)
    }

    // 전화번호 E.164 형식 변환
    private fun formatPhoneNumberToE164(phoneNumber: String, countryCode: String = "+82"): String {
        // 숫자만 남기기
        val cleanedNumber = phoneNumber.replace(Regex("[^\\d]"), "")
        return if (cleanedNumber.startsWith("0")) {
            countryCode + cleanedNumber.substring(1)
        } else {
            countryCode + cleanedNumber
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}