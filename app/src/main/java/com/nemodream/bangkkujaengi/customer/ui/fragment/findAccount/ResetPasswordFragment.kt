package com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.nemodream.bangkkujaengi.customer.ui.custom.CustomDialog
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ResetPasswordViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentResetPasswordBinding
import com.nemodream.bangkkujaengi.utils.hideKeyboard
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

        memberId = arguments?.getString("memberId")

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        // 빈 공간 터치 시 키보드 숨김 처리
        binding.root.setOnClickListener {
            binding.root.hideKeyboard()
        }

        // 입력 필드 변화 감지
        binding.tfResetPwPw.editText?.addTextChangedListener { validateFields() }
        binding.tfResetPwChkPw.editText?.addTextChangedListener { validateFields() }

        // 변경하기 버튼 클릭
        binding.btnResetPwChangePw.setOnClickListener {
            val password = binding.tfResetPwPw.editText?.text.toString()
            val confirmPassword = binding.tfResetPwChkPw.editText?.text.toString()

            if (password.length < 8) {
                Snackbar.make(binding.root, "비밀번호는 8자 이상 입력해주세요.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Snackbar.make(binding.root, "비밀번호가 서로 다릅니다.", Snackbar.LENGTH_SHORT).show()
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
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                requireActivity().finish() // 현재 액티비티 종료
            }
        }

        // 오류 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
