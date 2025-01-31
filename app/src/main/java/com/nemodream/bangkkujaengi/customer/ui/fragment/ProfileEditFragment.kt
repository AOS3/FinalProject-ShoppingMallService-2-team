package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.ui.adapter.GalleryAdapter
import com.nemodream.bangkkujaengi.customer.ui.adapter.OnImageClickListener
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.MyPageViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ProfileEditViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentProfileEditBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.loadImage
import com.nemodream.bangkkujaengi.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditFragment : Fragment(), OnImageClickListener {
    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels()

    private val args: ProfileEditFragmentArgs by navArgs()

    private val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            findNavController().navigateUp()
        }
    }

    private val galleryAdapter by lazy { GalleryAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        checkAndRequestPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onImageClick(uri: Uri) {
        viewModel.setSelectedImage(uri)
    }

    private fun setupUI() {
        with(binding) {
            args.currentProfileUrl?.let { url ->
                ivProfilePreview.loadImage(url)
            }

            rvGallery.adapter = galleryAdapter

            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_complete -> {
                        viewModel.saveProfileImage(requireContext().getUserId())
                        true
                    }
                    else -> false
                }
            }
        }

    }

    private fun observeViewModel() {
        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.ivProfilePreview.setImageURI(it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgress.root.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                requireContext().showSnackBar(binding.root, "프로필 이미지가 변경되었습니다.")
                setFragmentResult("updated_profile_image", Bundle().apply {
                    putBoolean("is_updated", true)
                    putString("profile_image_url", viewModel.uploadedImageUrl.value)
                })
                findNavController().navigateUp()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.galleryImages.collectLatest { pagingData ->
                galleryAdapter.submitData(pagingData)
            }
        }
    }

    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                requiredPermission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 있으면 PagingData가 자동으로 로드됨
            }
            else -> {
                requestPermissionLauncher.launch(requiredPermission)
            }
        }
    }
}