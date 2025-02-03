package com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.MaterialToolbar
import com.nemodream.bangkkujaengi.customer.ui.custom.CustomDialog
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ResetPasswordViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentResetPasswordBinding
import com.nemodream.bangkkujaengi.utils.clearFocusOnTouchOutside
import com.nemodream.bangkkujaengi.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResetPasswordViewModel by viewModels()

    private var memberId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: MaterialToolbar = binding.toolbarResetPw
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // 뒤로가기
        }

        view.setOnTouchListener { _, event ->
            clearFocusOnTouchOutside(event) // Fragment 확장 함수 호출
            false // 다른 터치 이벤트도 처리되도록 false 반환
        }

        memberId = arguments?.getString("memberId")

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        // 입력 필드 변화 감지
        binding.tfResetPwPw.editText?.addTextChangedListener { validateFields() }
        binding.tfResetPwChkPw.editText?.addTextChangedListener { validateFields() }

        // 변경하기 버튼 클릭
        binding.btnResetPwChangePw.setOnClickListener {
            val password = binding.tfResetPwPw.editText?.text.toString()
            val confirmPassword = binding.tfResetPwChkPw.editText?.text.toString()

            if (password.length < 8) {
                requireContext().showSnackBar(binding.root, "비밀번호는 8자 이상 입력해주세요.")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                requireContext().showSnackBar(binding.root, "비밀번호가 서로 다릅니다.")
                return@setOnClickListener
            }

            // 다이얼로그 띄우기
            CustomDialog(
                context = requireContext(),
                message = "비밀번호를 변경하시겠습니까?",
                onConfirm = {
                    memberId?.let {
                        viewModel.changePassword(it, password)
                    }
                },
                onCancel = {}
            ).show()
        }
    }

    private fun validateFields() {
        val password = binding.tfResetPwPw.editText?.text.toString()
        val confirmPassword = binding.tfResetPwChkPw.editText?.text.toString()
        viewModel.validateFields(password, confirmPassword)
    }

    private fun setupObservers() {
        // 버튼 활성화 상태 관찰
        viewModel.isButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnResetPwChangePw.isEnabled = isEnabled
        }

        // 성공 메시지 관찰
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                requireContext().showSnackBar(binding.root, it)
                requireActivity().finish() // 현재 액티비티 종료
            }
        }

        // 오류 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                requireContext().showSnackBar(binding.root, it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
