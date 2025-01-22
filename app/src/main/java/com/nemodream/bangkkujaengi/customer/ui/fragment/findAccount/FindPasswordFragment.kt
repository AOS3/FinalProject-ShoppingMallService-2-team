package com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentFindPasswordBinding
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.FindPasswordViewModel
import com.nemodream.bangkkujaengi.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindPasswordFragment : Fragment() {

    private var _binding: FragmentFindPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FindPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // 에러 메시지 처리
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        // 성공 메시지 처리
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        // 인증 상태 처리
        viewModel.isVerificationCodeSent.observe(viewLifecycleOwner) { isSent ->
            if (isSent) {
                binding.tfFindPwVerificationCode.visibility = View.VISIBLE
                binding.btnFindPwCheckVerification.visibility = View.VISIBLE
            }
        }

        // 인증 성공 여부 처리
        viewModel.isVerified.observe(viewLifecycleOwner) { isVerified ->
            binding.btnFindPwResetPw.visibility = if (isVerified) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setupListeners() {
        // 빈 공간 터치 시 키보드 숨김 처리
        binding.root.setOnClickListener {
            binding.root.hideKeyboard()
        }

        // 전화번호 입력 시 인증 요청 버튼 활성화
        binding.tfFindPwPhoneNumber.editText?.addTextChangedListener {
            binding.btnFindPwVerification.isEnabled = it?.isNotEmpty() == true
        }

        // 인증 요청 버튼 클릭
        binding.btnFindPwVerification.setOnClickListener {
            val id = binding.tfFindPwId.editText?.text.toString()
            val phoneNumber = binding.tfFindPwPhoneNumber.editText?.text.toString()

            if (id.isBlank() || phoneNumber.isBlank()) {
                Snackbar.make(binding.root, "아이디와 전화번호를 입력해주세요.", Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.requestVerification(id, phoneNumber)
            }
        }

        // 인증 확인 버튼 클릭
        binding.btnFindPwCheckVerification.setOnClickListener {
            binding.root.hideKeyboard()
            val inputCode = binding.tfFindPwVerificationCode.editText?.text.toString()

            if (inputCode.isBlank()) {
                Snackbar.make(binding.root, "인증번호를 입력해주세요.", Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.verifyCode(inputCode)
            }
        }

        // 비밀번호 재설정 버튼 클릭
        binding.btnFindPwResetPw.setOnClickListener {
            val memberId = viewModel.memberId.value
            if (!memberId.isNullOrEmpty()) {
                Log.d("FindPasswordFragment", "회원 ID: $memberId")
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
                Snackbar.make(binding.root, "인증되지 않은 사용자입니다.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}