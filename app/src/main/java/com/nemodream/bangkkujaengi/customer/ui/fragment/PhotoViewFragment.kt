package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nemodream.bangkkujaengi.databinding.FragmentPhotoViewBinding

class PhotoViewFragment: Fragment() {
    private var _binding: FragmentPhotoViewBinding? = null
    private val binding get() = _binding!!

    // 상태바 색상 변경을 위한 window 객체 초기화
    private val window: Window by lazy {
        activity?.window ?: throw IllegalStateException("Activity is null")
    }

    private val profileImageUri: String? by lazy {
        arguments?.getString("profileImageUri")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleStatusBarColor()
        setupUI()
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toggleStatusBarColor()
        _binding = null
    }

    /*
    * 상태바 색상 변경
    * */
    private fun toggleStatusBarColor() {
        val currentColor = window.statusBarColor
        when (currentColor) {
            Color.BLACK -> {
                window.statusBarColor = Color.WHITE
                WindowCompat.getInsetsController(window, requireView()).apply {
                    isAppearanceLightStatusBars = true  // 아이콘 검은색
                }
            }
            Color.WHITE -> {
                window.statusBarColor = Color.BLACK
                WindowCompat.getInsetsController(window, requireView()).apply {
                    isAppearanceLightStatusBars = false  // 아이콘 흰색
                }
            }
            else -> {
                window.statusBarColor = Color.BLACK
                WindowCompat.getInsetsController(window, requireView()).apply {
                    isAppearanceLightStatusBars = false  // 아이콘 흰색
                }
            }
        }
    }

    private fun setupUI() {
        with(binding) {
            profileImageUri?.let {
                ivProfileImage.setImageURI(it.toUri())
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnFinish.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}