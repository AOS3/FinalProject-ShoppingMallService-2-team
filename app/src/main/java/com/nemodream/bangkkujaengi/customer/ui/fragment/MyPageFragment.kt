package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SocialLogin
import com.nemodream.bangkkujaengi.customer.ui.adapter.ProductClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.PromotionProductAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.MyPageViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.NicknameUpdateUiState
import com.nemodream.bangkkujaengi.databinding.FragmentMyPageBinding
import com.nemodream.bangkkujaengi.utils.clearUserInfo
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.getUserType
import com.nemodream.bangkkujaengi.utils.hideKeyboard
import com.nemodream.bangkkujaengi.utils.loadImage
import com.nemodream.bangkkujaengi.utils.showKeyboard
import com.nemodream.bangkkujaengi.utils.showLoginSnackbar
import com.nemodream.bangkkujaengi.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPageFragment : Fragment(), ProductClickListener {
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext: Context

    private val viewModel: MyPageViewModel by viewModels()

    private val adapter: PromotionProductAdapter by lazy { PromotionProductAdapter(this) }
    private var profileImageUri: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadRecentProduct(appContext.getUserId())
        setupUI()
        setupListeners()
        observeViewModel()
        setupProfileEdit()
    }

    private fun observeViewModel() {
        viewModel.memberInfo.observe(viewLifecycleOwner) { member ->
            with(binding) {
                member.memberProfileImage?.let {
                    ivMyPageProfileImage.loadImage(it)
                    profileImageUri = it
                } ?: ivMyPageProfileImage.setImageResource(R.drawable.ic_default_profile)
                tvMyPageProfileName.text = "${member.memberNickName}님"

                ivMyPageSocialKakao.visibility = when (member.socialLogin) {
                    SocialLogin.KAKAO -> View.VISIBLE
                    else -> View.GONE
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                when (isLoading) {
                    true -> {
                        shimmerLayout.root.visibility = View.VISIBLE
                        shimmerLayout.root.startShimmer()
                        rvRecentViewProduct.visibility = View.INVISIBLE
                        tvRecentViewProductLabel.visibility = View.INVISIBLE
                    }

                    false -> {
                        shimmerLayout.root.visibility = View.INVISIBLE
                        shimmerLayout.root.stopShimmer()
                        rvRecentViewProduct.visibility = View.VISIBLE
                        tvRecentViewProductLabel.visibility = View.VISIBLE

                        if (viewModel.recentProductList.value.isNullOrEmpty()) {
                            tvRecentViewProductLabel.visibility = View.INVISIBLE
                            rvRecentViewProduct.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }

        viewModel.recentProductList.observe(viewLifecycleOwner) { recentItems ->
            if (recentItems.isNotEmpty()) {
                adapter.submitList(recentItems)
                return@observe
            }
            binding.tvRecentViewProductLabel.visibility = View.INVISIBLE
            binding.rvRecentViewProduct.visibility = View.INVISIBLE
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onProductClick(product: Product) {
        val action =
            MyPageFragmentDirections.actionNavigationMyPageToNavigationProductDetail(product.productId)
        findNavController().navigate(action)
    }

    override fun onFavoriteClick(product: Product) {
        when (appContext.getUserType()) {
            "member" -> {
                viewModel.toggleFavorite(appContext.getUserId(), product.productId)
            }

            "guest" -> {
                appContext.showLoginSnackbar(
                    binding.root,
                    requireActivity().findViewById(R.id.customer_bottom_navigation),
                ) {
                    val action = HomeFragmentDirections.actionNavigationHomeToSignInActivity()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupUI() {
        val userType = appContext.getUserType()

        with(binding) {
            // 멤버 타입에 따라 화면을 나눈다.
            viewModel.getMemberInfo(appContext.getUserId())
            when (userType) {
                "member" -> {
                    groupMyPageNonMember.visibility = View.INVISIBLE
                    groupMyPageMember.visibility = View.VISIBLE
                    tvMyPageLogout.visibility = View.VISIBLE
                }

                "guest" -> {
                    groupMyPageNonMember.visibility = View.VISIBLE
                    groupMyPageMember.visibility = View.INVISIBLE
                    tvMyPageLogout.visibility = View.INVISIBLE
                }
            }

            // '3000원'을 텍스트 스타일을 볼드체로 변경
            SpannableString(tvMyPageLoginSubTitle.text).apply {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    tvMyPageLoginSubTitle.text.indexOf("3000"),
                    tvMyPageLoginSubTitle.text.indexOf("3000") + "3000".length + 1,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                tvMyPageLoginSubTitle.text = this
            }

            rvRecentViewProduct.adapter = adapter
        }
    }

    private fun setupListeners() {
        with(binding) {
            viewMyPageLogin.setOnClickListener {
                val action = MyPageFragmentDirections.actionNavigationMyPageToSignInActivity()
                findNavController().navigate(action)
            }

            tvMyPageNonMemberInquiry.setOnClickListener {
                val action =
                    MyPageFragmentDirections.actionNavigationMyPageToNavigationNonMemberOrder()
                findNavController().navigate(action)
            }

            toolbarMyPage.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_cart -> {
                        val action =
                            MyPageFragmentDirections.actionNavigationMyPageToNavigationCart()
                        findNavController().navigate(action)
                        true
                    }

                    else -> false
                }
            }

            ivMyPageProfileImage.setOnClickListener {
                ProfileBottomSheet().apply {
                    arguments = Bundle().apply {
                        putString("profileImageUri", profileImageUri)
                    }
                }.show(childFragmentManager, ProfileBottomSheet.TAG)
            }

            setFragmentResultListener("updated_profile_image") { _, bundle ->
                val profileImageUrl = bundle.getString("profile_image_url")
                profileImageUrl?.let { url ->
                    binding.ivMyPageProfileImage.loadImage(url)
                }
            }

            etMyProfileNickname.setOnEditorActionListener { textView, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    textView.hideKeyboard()
                    viewModel.updateNickname(requireContext().getUserId(), etMyProfileNickname.text.toString())
                    true
                } else {
                    false
                }
            }

            myPageOrder.root.setOnClickListener { }
            myPageReview.root.setOnClickListener { }
            myPageCoupon.root.setOnClickListener {
                val action = MyPageFragmentDirections.actionNavigationMyPageToNavigationMyCoupon("")
                findNavController().navigate(action)
            }
            myPageReserve.root.setOnClickListener { }
            myPageBookmark.root.setOnClickListener {
                val action = MyPageFragmentDirections.actionNavigationMyPageToMyBookmarkFragment()
                findNavController().navigate(action)
            }


            tvMyPageSetting.setOnClickListener {
                appContext.showToast("준비중입니다.")
            }
            tvMyPageContact.setOnClickListener {
                appContext.showToast("준비중입니다.")
            }
            tvMyPageCenter.setOnClickListener {
                appContext.showToast("준비중입니다.")
            }
            tvMyPageNotice.setOnClickListener {
                appContext.showToast("준비중입니다.")
            }

            tvMyPageLogout.setOnClickListener {
                appContext.clearUserInfo()
                val action = MyPageFragmentDirections.actionNavigationMyPageToSignInActivity()
                findNavController().navigate(action)
                requireActivity().finish()
            }
        }
    }


    // 수정 모드에서 일반 모드로 전환
    private fun setupProfileEdit() {
        with(binding) {
            // 초기 상태 설정
            etMyProfileNickname.visibility = View.GONE
            btnSocialMySaveEdit.visibility = View.GONE

            btnProfileNameEdit.setOnClickListener {
                // 수정 모드로 전환
                etMyProfileNickname.apply {
                    setText(viewModel.memberInfo.value?.memberNickName)
                    visibility = View.VISIBLE
                    setPadding(8, 0, 0, 20)
                    requestFocus()
                    setSelection(text.length)
                    requireContext().showKeyboard(this)

                }
                tvMyPageProfileName.visibility = View.GONE
                btnProfileNameEdit.visibility = View.GONE
                btnSocialMySaveEdit.visibility = View.VISIBLE
            }

            btnSocialMySaveEdit.setOnClickListener {
                viewModel.updateNickname(requireContext().getUserId(), etMyProfileNickname.text.toString())
                requireContext().hideKeyboard(it)
            }

            viewModel.nicknameUpdateState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is NicknameUpdateUiState.Success -> {
                        etMyProfileNickname.visibility = View.GONE
                        tvMyPageProfileName.visibility = View.VISIBLE
                        btnProfileNameEdit.visibility = View.VISIBLE
                        btnSocialMySaveEdit.visibility = View.GONE
                        Snackbar.make(root, state.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is NicknameUpdateUiState.Error -> {
                        Snackbar.make(root, state.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}