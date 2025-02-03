package com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentFindPasswordBinding
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.FindPasswordViewModel
import com.nemodream.bangkkujaengi.utils.clearFocusOnTouchOutside
import com.nemodream.bangkkujaengi.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class FindPasswordFragment : Fragment() {

    private var _binding: FragmentFindPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FindPasswordViewModel by viewModels()

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        view.setOnTouchListener { _, event ->
            clearFocusOnTouchOutside(event) // Fragment 확장 함수 호출
            false // 다른 터치 이벤트도 처리되도록 false 반환
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // 에러 메시지 처리
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                requireContext().showSnackBar(binding.root, it)
                viewModel.clearErrorMessage() // 메시지 초기화
            }
        }

        // 성공 메시지 처리
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                requireContext().showSnackBar(binding.root, it)
                viewModel.clearSuccessMessage() // 메시지 초기화
            }
        }

        // 인증 상태 처리
        viewModel.isVerificationCodeSent.observe(viewLifecycleOwner) { isSent ->
            if (isSent) {
                binding.tfFindPwVerificationCode.visibility = View.VISIBLE
                binding.btnFindPwCheckVerification.visibility = View.VISIBLE
            }
        }

        viewModel.isVerified.observe(viewLifecycleOwner) { isVerified ->
            if (isVerified) {
                binding.btnFindPwResetPw.visibility = View.VISIBLE
                binding.btnFindPwResetPw.isEnabled = true
            } else {
                binding.btnFindPwResetPw.visibility = View.INVISIBLE
                binding.btnFindPwResetPw.isEnabled = false
            }
        }


        // 인증 요청 버튼 활성화 상태 관찰
        viewModel.isVerificationButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnFindPwVerification.isEnabled = isEnabled
        }

        // 인증 확인 버튼 활성화 상태 관찰
        viewModel.isVerificationCheckButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnFindPwCheckVerification.isEnabled = isEnabled
        }
    }

    private fun setupListeners() {
        // 아이디 입력 감지
        binding.tfFindPwId.editText?.addTextChangedListener { text ->
            viewModel.idInput.value = text?.toString()
            viewModel.updateButtonStates()
        }

        // 전화번호 입력 감지
        binding.tfFindPwPhoneNumber.editText?.addTextChangedListener { text ->
            viewModel.phoneNumberInput.value = text?.toString()
            viewModel.updateButtonStates()
        }

        // 인증번호 입력 감지
        binding.tfFindPwVerificationCode.editText?.addTextChangedListener { text ->
            viewModel.verificationCodeInput.value = text?.toString()
            viewModel.updateVerificationCheckButtonState()
        }

        // 인증 요청 버튼 클릭
        binding.btnFindPwVerification.setOnClickListener {
            val id = binding.tfFindPwId.editText?.text.toString()
            val phoneNumber = binding.tfFindPwPhoneNumber.editText?.text.toString()

            if (id.isBlank() || phoneNumber.isBlank()) {
                requireContext().showSnackBar(binding.root, "아이디와 전화번호를 입력해주세요.")
                return@setOnClickListener
            }

            // ViewModel에서 인증 요청
            viewModel.requestVerification(id, phoneNumber)

            // 인증 코드 발송 여부 관찰
            viewModel.isVerificationCodeSent.observe(viewLifecycleOwner) { isSent ->
                if (isSent) {
                    val formattedPhoneNumber = formatPhoneNumberToE164(phoneNumber)
                    if (formattedPhoneNumber.isNotEmpty()) {
                        startPhoneNumberVerification(formattedPhoneNumber)
                    } else {
                        requireContext().showSnackBar(binding.root, "전화번호 형식이 잘못되었습니다.")
                    }
                }
            }

            // 성공 메시지 관찰
            viewModel.successMessage.observe(viewLifecycleOwner) { message ->
                message?.let {
                    requireContext().showSnackBar(binding.root, it)
                    viewModel.clearSuccessMessage()
                }
            }
        }

        // 인증 확인 버튼 클릭
        binding.btnFindPwCheckVerification.setOnClickListener {
            val inputCode = binding.tfFindPwVerificationCode.editText?.text.toString()

            if (inputCode.isBlank() || storedVerificationId == null) {
                requireContext().showSnackBar(binding.root, "인증번호를 입력하거나 인증 요청을 다시 진행하세요.")
                return@setOnClickListener
            }

            binding.btnFindPwCheckVerification.isEnabled = false // 버튼 비활성화
            verifyCode(inputCode)
        }

        // 비밀번호 재설정 버튼 클릭
        binding.btnFindPwResetPw.setOnClickListener {
            val memberId = viewModel.memberId.value
            if (!memberId.isNullOrEmpty()) {
                val bundle = Bundle().apply {
                    putString("memberId", memberId)
                }
                val fragment = ResetPasswordFragment().apply {
                    arguments = bundle
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_find_info, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                requireContext().showSnackBar(binding.root, "인증되지 않은 사용자입니다.")
            }
        }

        // 인증 성공 여부 관찰
        viewModel.isVerified.observe(viewLifecycleOwner) { isVerified ->
            binding.btnFindPwResetPw.visibility = if (isVerified) View.VISIBLE else View.INVISIBLE
        }

    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber) // E.164 형식의 전화번호
            .setTimeout(60L, TimeUnit.SECONDS) // 인증 타임아웃 설정
            .setActivity(requireActivity()) // 현재 액티비티
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d("PhoneAuth", "Verification completed")
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    requireContext().showSnackBar(binding.root, "인증번호 전송 실패")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    requireContext().showSnackBar(binding.root, "인증번호가 전송되었습니다.")

                    binding.btnFindPwVerification.postDelayed({
                        binding.btnFindPwVerification.isEnabled = true
                    }, 60000)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun formatPhoneNumberToE164(phoneNumber: String, countryCode: String = "+82"): String {
        // 숫자만 남기기
        val cleanedNumber = phoneNumber.replace(Regex("[^\\d]"), "")
        return if (cleanedNumber.startsWith("0")) {
            countryCode + cleanedNumber.substring(1)
        } else {
            countryCode + cleanedNumber
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    requireContext().showSnackBar(binding.root, "전화번호 인증 성공!")
                    viewModel.onVerificationSuccess() // 인증 성공 처리
                } else {
                    requireContext().showSnackBar(binding.root, "전화번호 인증 실패: ${task.exception?.message}")
                    binding.btnFindPwCheckVerification.isEnabled = true // 실패 시 버튼 다시 활성화
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}