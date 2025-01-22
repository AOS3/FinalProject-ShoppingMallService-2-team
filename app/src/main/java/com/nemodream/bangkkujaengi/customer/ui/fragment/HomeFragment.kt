package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Banner
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.ui.adapter.HomeBannerAdapter
import com.nemodream.bangkkujaengi.customer.ui.adapter.MoreProductsClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.OnBannerItemClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.ProductClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.PromotionAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.HomeViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), OnBannerItemClickListener, ProductClickListener,
    MoreProductsClickListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val bannerAdapter: HomeBannerAdapter by lazy { HomeBannerAdapter(this) }
    private val promotionAdapter: PromotionAdapter by lazy { PromotionAdapter(this, this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupLayout()
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /*
    * 배너 클릭 리스너
    * */
    override fun onItemClick(banner: Banner) {
        val action =
            HomeFragmentDirections.actionNavigationHomeToNavigationProductDetail(banner.productId)
        findNavController().navigate(action)
    }

    /*
    * HomeViewModel에서 데이터 불러와
    * 변경이 감지되면 데이터 갱신
    * */
    private fun observeViewModel() {
        viewModel.bannerItems.observe(viewLifecycleOwner) { bannerList ->
            bannerAdapter.submitList(bannerList)
        }

        viewModel.bannerLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                when (isLoading) {
                    true -> {
                        shimmerLayout.visibility = View.VISIBLE
                        shimmerLayout.startShimmer()
                        viewPagerCarousel.visibility = View.INVISIBLE
                        tabIndicator.visibility = View.GONE
                    }

                    false -> {
                        shimmerLayout.visibility = View.INVISIBLE
                        shimmerLayout.stopShimmer()
                        viewPagerCarousel.visibility = View.VISIBLE
                        tabIndicator.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewModel.promotionItems.observe(viewLifecycleOwner) { items ->
            promotionAdapter.submitList(items)
        }

        viewModel.promotionLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                when (isLoading) {
                    true -> {
                        shimmerLayout.startShimmer()
                        promotionShimmerLayout.root.visibility = View.VISIBLE
                        rvPromotion.visibility = View.GONE
                    }
                    false -> {
                        shimmerLayout.startShimmer()
                        promotionShimmerLayout.root.visibility = View.GONE
                        rvPromotion.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    /*
    * 화면 UI 배치 관련 함수 모음
    * */
    private fun setupLayout() {
        viewModel.loadBannerItems()
        viewModel.loadPromotions()
        setupHomeBannerUI()
        binding.rvPromotion.adapter = promotionAdapter
    }

    /*
    * 홈 배너 화면
    * */
    private fun setupHomeBannerUI() {
        with(binding) {
            viewPagerCarousel.adapter = bannerAdapter
            // TabLayout과 ViewPager2 연동
            TabLayoutMediator(tabIndicator, viewPagerCarousel) { _, _ -> }
                .attach()
        }

    }

    /*
    * 리스너 모음 함수
    * */
    private fun setupListeners() {
        with(binding) {
            categoryFurniture.root.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionNavigationHomeToCategoryProductFragment(
                        CategoryType.FURNITURE
                    )
                findNavController().navigate(action)
            }

            categoryLighting.root.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionNavigationHomeToCategoryProductFragment(
                        CategoryType.LIGHTING
                    )
                findNavController().navigate(action)
            }

            categoryFabric.root.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionNavigationHomeToCategoryProductFragment(
                        CategoryType.FABRIC
                    )
                findNavController().navigate(action)
            }

            categoryDeco.root.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionNavigationHomeToCategoryProductFragment(
                        CategoryType.DECO
                    )
                findNavController().navigate(action)
            }

            // 각 메뉴 버튼에 따라서 화면 이동하기
            toolbarHome.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_search -> {
                        val action =
                            HomeFragmentDirections.actionNavigationHomeToNavigationSearch()
                        findNavController().navigate(action)
                        true
                    }

                    R.id.menu_cart -> {
                        val action =
                            HomeFragmentDirections.actionNavigationHomeToNavigationCart()
                        findNavController().navigate(action)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    override fun onProductClick(product: Product) {
        val action =
            HomeFragmentDirections.actionNavigationHomeToNavigationProductDetail(product.productId)
        findNavController().navigate(action)
    }

    override fun onMoreProductsClick(title: String) {
        val action = HomeFragmentDirections.actionNavigationHomeToNavigationPromotion(title)
        findNavController().navigate(action)
    }
}