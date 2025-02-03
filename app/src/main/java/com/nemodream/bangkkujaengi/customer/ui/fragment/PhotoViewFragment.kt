package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentPhotoViewBinding
import com.nemodream.bangkkujaengi.utils.loadImage

class PhotoViewFragment: Fragment() {
    private var _binding: FragmentPhotoViewBinding? = null
    private val binding get() = _binding!!

    private val args: PhotoViewFragmentArgs by navArgs()

    private val window: Window by lazy {
        activity?.window ?: throw IllegalStateException("Activity is null")
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
            args.profileImageUrl?.let { url ->
                ivProfileImage.loadImage(url)
            } ?: run {
                ivProfileImage.setImageResource(R.drawable.ic_default_profile)
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