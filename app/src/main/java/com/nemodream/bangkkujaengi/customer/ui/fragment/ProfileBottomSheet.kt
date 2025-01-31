package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nemodream.bangkkujaengi.databinding.BottomSheetProfileBinding

class ProfileBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.tvProfileChange.setOnClickListener {
            // 프로필 사진 변경 처리
            dismiss()
        }

        binding.tvProfileView.setOnClickListener {
            val profileImageUri = arguments?.getString("profileImageUri")
            val action = MyPageFragmentDirections.actionNavigationMyPageToPhotoViewFragment(profileImageUri)
            findNavController().navigate(action)
            dismiss()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ProfileBottomSheet"
    }
}