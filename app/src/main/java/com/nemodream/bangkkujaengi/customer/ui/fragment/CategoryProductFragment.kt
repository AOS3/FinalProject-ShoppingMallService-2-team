package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SortType
import com.nemodream.bangkkujaengi.customer.data.model.SubCategoryType
import com.nemodream.bangkkujaengi.customer.ui.adapter.ProductClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.ProductGridAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.CategoryProductViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentCategoryProductBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.getUserType
import com.nemodream.bangkkujaengi.utils.showLoginSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryProductFragment: Fragment(), ProductClickListener {
    private var _binding: FragmentCategoryProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext: Context

    private val args: CategoryProductFragmentArgs by navArgs()

    private val viewModel: CategoryProductViewModel by viewModels()

    private val adapter: ProductGridAdapter by lazy { ProductGridAdapter(this) }
    private val categoryType: CategoryType by lazy { args.category }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setUserId(appContext.getUserId())
        setupUI()
        observeViewModel()
        setupTabs()
        setupListeners()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onProductClick(product: Product) {
        val action = CategoryProductFragmentDirections.actionNavigationCategoryToNavigationProductDetail(product.productId)
        findNavController().navigate(action)
    }

    override fun onFavoriteClick(product: Product) {
        when(appContext.getUserType()) {
            "member" -> {
            viewModel.toggleFavorite(appContext.getUserId(), product.productId)
            }
            "guest" -> {
                appContext.showLoginSnackbar(binding.root) {
                    val action = CategoryProductFragmentDirections.actionNavigationCategoryToSignInActivity()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupUI() {
        binding.rvCategoryProductList.adapter = adapter
        binding.rvCategoryProductList.itemAnimator = null
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }

        viewModel.sortText.observe(viewLifecycleOwner) { sortText ->
            binding.chipPromotionSort.text = sortText
        }

        viewModel.productLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                shimmerCategoryProduct.root.visibility = if (isLoading) View.VISIBLE else View.GONE
                rvCategoryProductList.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }

    /*
    * 리스너 모음 함수
    * */
    private fun setupListeners() {
        with(binding) {
            toolbarCategoryProduct.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            tabCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        setupSubCategoryChips(CategoryType.entries[it.position])
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }

        // 각 메뉴 버튼에 따라서 화면 이동하기
        binding.toolbarCategoryProduct.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_search -> {
                    val action = CategoryProductFragmentDirections.actionNavigationCategoryToNavigationSearch()
                    findNavController().navigate(action)
                    true
                }
                R.id.menu_cart -> {
                    val action = CategoryProductFragmentDirections.actionNavigationCategoryToNavigationCart()
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }

        binding.chipPromotionSort.setOnClickListener {
            showSortPopup(it)
        }
    }

    /*
    * 카테고리별로 탭을 구성하고
    * 전달 받은 categoryType에 해당하는 탭을 선택한다.
    * */
    private fun setupTabs() {
        // 탭 추가
        CategoryType.entries.forEach { type ->
            binding.tabCategory.addTab(binding.tabCategory.newTab().setText(type.getTabTitle()))
        }

        // 초기 데이터 로드와 탭 선택
        categoryType.let { initialType ->
            val position = initialType.ordinal
            setupSubCategoryChips(initialType)
            binding.tabCategory.getTabAt(position)?.select()
        }

    }

    fun setupSubCategoryChips(categoryType: CategoryType) {
        binding.chipGroupSubCategory.removeAllViews()
        val subCategories = SubCategoryType.getSubCategories(categoryType)

        // 하위 카테고리가 있을 때만 칩을 생성
        if (subCategories.isNotEmpty()) {
            subCategories.forEach { subCategory ->
                val chip = Chip(appContext).apply {
                    text = subCategory.title
                    tag = subCategory  // SubCategoryType을 tag로 저장

                    chipCornerRadius = resources.getDimension(R.dimen.chip_corner_radius)
                    setChipBackgroundColorResource(R.color.white)
                    chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
                    setChipStrokeColorResource(R.color.black)
                    typeface = Typeface.DEFAULT_BOLD

                    setOnClickListener {
                        updateChipSelection(this)
                        viewModel.updateSubCategory(tag as SubCategoryType)
                    }
                }
                binding.chipGroupSubCategory.addView(chip)
            }

            // 첫 번째 칩 선택 상태로 만들기만 하고 updateSubCategory는 호출하지 않음
            binding.chipGroupSubCategory.getChildAt(0)?.let { firstChip ->
                updateChipSelection(firstChip as Chip)
            }
        }

        viewModel.updateCategory(categoryType)  // 한 번만 호출
    }

    private fun updateChipSelection(selectedChip: Chip) {
        // ChipGroup의 모든 Chip 순회하며 스타일 업데이트
        with(binding) {
            (0 until chipGroupSubCategory.childCount).forEach { index ->
                when (val chip = chipGroupSubCategory.getChildAt(index) as Chip) {
                    selectedChip -> {
                        // 선택된 Chip 스타일
                        chip.setChipBackgroundColorResource(R.color.black)
                        chip.setTextColor(ContextCompat.getColor(appContext, R.color.white))
                    }
                    else -> {
                        // 선택되지 않은 Chip 스타일
                        chip.setChipBackgroundColorResource(R.color.white)
                        chip.setTextColor(ContextCompat.getColor(appContext, R.color.black))
                        chip.typeface = Typeface.DEFAULT
                    }
                }
            }
        }

    }

    private fun showSortPopup(view: View) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(R.menu.product_sort_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.sort_purchase -> {
                        viewModel.updateSort(SortType.PURCHASE)
                        true
                    }
                    R.id.sort_review -> {
                        viewModel.updateSort(SortType.REVIEW)
                        true
                    }
                    R.id.sort_price_high -> {
                        viewModel.updateSort(SortType.PRICE_HIGH)
                        true
                    }
                    R.id.sort_price_low -> {
                        viewModel.updateSort(SortType.PRICE_LOW)
                        true
                    }
                    R.id.sort_discount -> {
                        viewModel.updateSort(SortType.DISCOUNT)
                        true
                    }
                    R.id.sort_views -> {
                        viewModel.updateSort(SortType.VIEWS)
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

}