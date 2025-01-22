package com.nemodream.bangkkujaengi.admin.ui.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nemodream.bangkkujaengi.admin.ui.adapter.AdminProductColorAdapter
import com.nemodream.bangkkujaengi.admin.ui.adapter.AdminProductImageAdapter
import com.nemodream.bangkkujaengi.admin.ui.adapter.OnImageCancelClickListener
import com.nemodream.bangkkujaengi.admin.ui.custom.CustomTextFieldDialog
import com.nemodream.bangkkujaengi.admin.ui.viewmodel.AdminEditProductViewModel
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.databinding.FragmentAdminEditProductBinding
import com.nemodream.bangkkujaengi.utils.createDropDownAdapter
import com.nemodream.bangkkujaengi.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminEditProductFragment: Fragment(), OnImageCancelClickListener {
    private var _binding: FragmentAdminEditProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext: Context

    private val args: AdminEditProductFragmentArgs by navArgs()
    private val productId by lazy { args.product.productId }

    private val viewModel: AdminEditProductViewModel by viewModels()
    private val imageAdapter: AdminProductImageAdapter by lazy { AdminProductImageAdapter(this) }
    private val colorAdapter: AdminProductColorAdapter by lazy { AdminProductColorAdapter() }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openPhotoPicker()
        }
    }

    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_IMAGE_COUNT)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addNewImages(uris)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadProduct(productId)
        setupUI()
        setupListeners()
        setupTextChangeListeners()
        observeViewModel()
        setupCategoryAndSubCategoryDropdown()
    }

    private fun setupUI() {
        with(binding) {
            rvAdminProductEditImage.adapter = imageAdapter
            rvProductEditColor.adapter = colorAdapter
            tfAdminProductEditDiscountPrice.editText?.isEnabled = false
        }


    }

    private fun setupListeners() {
        with(binding) {
            toolbarAdminProductEdit.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            viewProductEditImageArea.setOnClickListener {
                checkAndRequestPermission()
            }

            btnProductEditSubmit.setOnClickListener {
                btnProductEditSubmit.isEnabled = false
                viewModel.updateProduct(
                    title = tfAdminProductEditTitle.editText?.text.toString(),
                    description = tfAdminProductEditDescription.editText?.text.toString(),
                    price = tfAdminProductEditPrice.editText?.text.toString(),
                    discountRate = tfAdminProductEditDiscountRate.editText?.text.toString(),
                    count = tfAdminProductEditCount.editText?.text.toString()
                )
            }

            btnProductEditColor.setOnClickListener {
                CustomTextFieldDialog(
                    context = requireContext(),
                    message = "색상을 입력하세요",
                    hint = "색상",
                    onConfirm = { color ->
                        if (color.isNotEmpty()) {
                            viewModel.addColor(color)
                        }
                    },
                    onCancel = {}
                ).show()
            }
        }
    }

    private fun setupTextChangeListeners() {
        with(binding) {
            tfAdminProductEditTitle.editText?.doAfterTextChanged {
                validateFields()
            }
            tfAdminProductEditDescription.editText?.doAfterTextChanged {
                validateFields()
            }
            tfAdminProductEditPrice.editText?.doAfterTextChanged { price ->
                viewModel.calculateDiscountPrice(
                    price.toString(),
                    tfAdminProductEditDiscountRate.editText?.text.toString()
                )
                validateFields()
            }
            tfAdminProductEditDiscountRate.editText?.doAfterTextChanged { rate ->
                viewModel.calculateDiscountPrice(
                    tfAdminProductEditPrice.editText?.text.toString(),
                    rate.toString()
                )
                validateFields()
            }
            tfAdminProductEditCount.editText?.doAfterTextChanged {
                validateFields()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewModel.displayImages.observe(viewLifecycleOwner) { uris ->
                imageAdapter.submitList(uris)
                updateImageLabel(uris.size)
            }

            viewModel.colors.observe(viewLifecycleOwner) { colors ->
                colorAdapter.submitList(colors)
            }

            viewModel.isSubmitEnabled.observe(viewLifecycleOwner) { isEnabled ->
                binding.btnProductEditSubmit.isEnabled = isEnabled
            }

            viewModel.discountPrice.observe(viewLifecycleOwner) { price ->
                binding.tfAdminProductEditDiscountPrice.editText?.setText(price)
            }

            viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
                when (isSuccess) {
                    true -> {
                        requireContext().showToast("상품이 성공적으로 수정되었습니다")
                        binding.btnProductEditSubmit.isEnabled = false
                        findNavController().navigateUp()
                    }
                    false -> {
                        requireContext().showToast("상품 수정에 실패했습니다")
                        binding.btnProductEditSubmit.isEnabled = true
                    }
                }
            }

            viewModel.product.observe(viewLifecycleOwner) { product ->
                with(binding) {
                    tfAdminProductEditTitle.editText?.setText(product.productName)
                    tfAdminProductEditDescription.editText?.setText(product.description)

                    autoCompleteCategory.setText(product.category.getTabTitle(), false)
                    updateSubCategoryDropdown(product.category)
                    autoCompleteSubCategory.setText(product.subCategory.title, false)

                    tfAdminProductEditPrice.editText?.setText(product.price.toString())
                    tfAdminProductEditDiscountRate.editText?.setText(product.saleRate.toString())
                    tfAdminProductEditCount.editText?.setText(product.productCount.toString())
                }
            }
        }
    }

    private fun setupCategoryAndSubCategoryDropdown() {
        val categories = viewModel.getCategories()

        binding.autoCompleteCategory.apply {
            setAdapter(appContext.createDropDownAdapter(categories.map { it.getTabTitle() }))
            setOnItemClickListener { _, _, position, _ ->
                val selectedCategory = categories[position]
                viewModel.setCategory(selectedCategory)
                updateSubCategoryDropdown(selectedCategory)
                validateFields()
            }
        }
    }

    private fun updateSubCategoryDropdown(categoryType: CategoryType) {
        clearSubCategory()
        val subCategories = viewModel.getSubCategories(categoryType)

        binding.autoCompleteSubCategory.apply {
            setAdapter(appContext.createDropDownAdapter(subCategories.map { it.title }))
            isEnabled = subCategories.isNotEmpty()
            setOnItemClickListener { _, _, position, _ ->
                viewModel.setSubCategory(subCategories[position])
                validateFields()
            }
        }
    }

    private fun clearSubCategory() {
        binding.autoCompleteSubCategory.apply {
            setText("")
            isEnabled = false
        }
        viewModel.setSubCategory(null)
        validateFields()
    }

    private fun validateFields() {
        with(binding) {
            viewModel.validateFields(
                tfAdminProductEditTitle.editText?.text.toString(),
                tfAdminProductEditDescription.editText?.text.toString(),
                tfAdminProductEditPrice.editText?.text.toString(),
                tfAdminProductEditDiscountRate.editText?.text.toString(),
                tfAdminProductEditCount.editText?.text.toString()
            )
        }
    }

    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            ) == PackageManager.PERMISSION_GRANTED -> {
                openPhotoPicker()
            }
            else -> {
                requestPermissionLauncher.launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                )
            }
        }
    }

    private fun openPhotoPicker() {
        if ((viewModel.displayImages.value?.size ?: 0) >= MAX_IMAGE_COUNT) {
            requireContext().showToast("이미지는 최대 ${MAX_IMAGE_COUNT}개까지만 선택 가능합니다")
            return
        }
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun updateImageLabel(size: Int) {
        binding.tvProductEditImageLabel.text =
            String.format("이미지 (%d / %d)", size, MAX_IMAGE_COUNT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCancelClick(uri: Uri) {
        viewModel.removeImage(uri)
        validateFields()
    }

    companion object {
        private const val MAX_IMAGE_COUNT = 5
    }
}