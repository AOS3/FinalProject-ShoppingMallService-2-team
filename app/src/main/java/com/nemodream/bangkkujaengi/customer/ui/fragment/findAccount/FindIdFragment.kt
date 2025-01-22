package com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.FindIdViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentFindIdBinding
import com.nemodream.bangkkujaengi.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FindIdFragment : Fragment() {

    private var _binding: FragmentFindIdBinding? = null
    private val binding get() = _binding!!

    private val findIdViewModel: FindIdViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // 아이디 조회 결과 관찰
        findIdViewModel.userId.observe(viewLifecycleOwner) { userInfo ->
            if (userInfo != null) {
                val (name, userId) = userInfo
                binding.tvFindIdShowId.apply {
                    text = "${name}님의 아이디 \n\n${userId}"
                    visibility = View.VISIBLE // TextView를 보이게 설정
                }
                binding.btnFindIdLogin.visibility = View.VISIBLE // 로그인 버튼 활성화
            }
        }

        // 오류 메시지 관찰
        findIdViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupListeners() {
        // 빈 공간 터치 시 키보드 숨김 처리
        binding.root.setOnClickListener {
            binding.root.hideKeyboard()
        }

        // 아이디 찾기 버튼 클릭
        binding.btnFindIdFindId.setOnClickListener {
            binding.root.hideKeyboard()
            val name = binding.tfFindIdName.editText?.text.toString()
            val phoneNumber = binding.tfFindIdPhoneNumber.editText?.text.toString()

            if (name.isBlank() || phoneNumber.isBlank()) {
                Toast.makeText(requireContext(), "이름과 전화번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                findIdViewModel.findId(name, phoneNumber)
            }
        }

        // 로그인 화면으로 이동
        binding.btnFindIdLogin.setOnClickListener {
            requireActivity().finish()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
