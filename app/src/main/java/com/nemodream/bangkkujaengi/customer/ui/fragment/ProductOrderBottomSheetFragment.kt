package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.PaymentItems
import com.nemodream.bangkkujaengi.customer.data.model.PaymentProduct
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.repository.ShoppingCartRepository
import com.nemodream.bangkkujaengi.customer.ui.custom.CustomDialog
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ProductDetailViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentProductOrderBottomSheetBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.getUserType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ProductOrderBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentProductOrderBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext: Context

    private val viewModel: ProductDetailViewModel by viewModels()

    private var selectedColor: String? = null

    private val product: Product by lazy {
        requireArguments().getParcelable(PRODUCT_KEY)!!
    }

    private var cartClickListener: OnCartClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(appContext, theme).apply {
            behavior.apply {
                isDraggable = true
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductOrderBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetUI()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * BottomSheet의 UI를 초기화하고 설정하는 함수
     * - Dialog 창 크기 및 배경 설정
     * - 색상 선택을 위한 DropDown 설정
     * - 색상 선택 이벤트 처리
     */
    private fun setupBottomSheetUI() {
        setupDialogWindow()
        setupColorDropdown()
        handleColorSelection()
    }

    /**
     * Dialog 창의 크기와 배경을 설정하는 함수
     */
    private fun setupDialogWindow() {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    /**
     * 색상 선택을 위한 DropDown 메뉴를 설정하는 함수
     */
    private fun setupColorDropdown() {
        ArrayAdapter(
            appContext,
            R.layout.item_dropdown_category,
            product.colors
        ).apply {
            binding.autoCompleteTextView.setAdapter(this)
        }
    }

    /**
     * 색상 선택 시 이벤트를 처리하는 함수
     * - 선택된 색상을 ViewModel에 업데이트
     * - 에러 메시지 숨김 처리
     */
    private fun handleColorSelection() {
        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedColor = product.colors[position]
            viewModel.updateSelectedColor(selectedColor)
            binding.tvProductOrderColorErrorMessage.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnCart.setOnClickListener {
                // 선택된 색상이 없으면 에러 메시지 표시 후 early return
                if (selectedColor == null) {
                    tvProductOrderColorErrorMessage.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                // 장바구니 정보 저장하는 함수
                viewLifecycleOwner.lifecycleScope.launch {
                    val userType = requireContext().getUserType()
                    val userId = if (userType == "member") {
                        withContext(Dispatchers.IO) {
                            ShoppingCartRepository.getting_user_id_by_document_id(requireContext().getUserId())
                        }
                    } else {
                        requireContext().getUserId()
                    }
                    Log.d("test1213", "setting_user_id: $userId")

                    val work1 = withContext(Dispatchers.IO) {
                        ShoppingCartRepository.add_cart_item_by_product_id(
                            userId,
                            product.productId,
                            viewModel.quantity.value!!,
                            viewModel.selectedColor.value!!,
                        )
                    }

                    showActionDialog()
                }
            }

            // 주문하기 버튼 클릭 리스너
            btnOrder.setOnClickListener {
                var user_id = ""
                var user_type = requireContext().getUserType()
                when(user_type) {
                    "member" -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val work1 = async(Dispatchers.IO) {
                                ShoppingCartRepository.getting_user_id_by_document_id(requireContext().getUserId())
                            }
                            user_id = work1.await()
                            Log.d("test1213", "setting_user_id: ${user_id}")
                        }
                    }
                    "guest" -> {
                        user_id = requireContext().getUserId()
                        Log.d("test1213", "setting_user_id: ${user_id}")
                    }
                    else -> {
                        ""
                    }
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val work1 = async(Dispatchers.IO) {
                        ShoppingCartRepository.getting_user_data_by_user_id(user_id)
                    }
                    val user_data = work1.await()

                    val memberData = user_data?.get("member_data") as? Member

                    Log.d("user_data", "${memberData?.memberPhoneNumber}")

                    val productData = PaymentProduct(
                        items = listOf(
                            PaymentItems(
                                productId = product.productId, // 원하는 상품 ID
                                color = viewModel.selectedColor.value!!,                // 원하는 색상
                                quantity = viewModel.quantity.value!!,                 // 원하는 수량
                                checked = true               // 선택 여부 (예: false)
                            )
                        ),
                        userId = user_id // 실제 사용자 ID
                    )

                    Log.d("123123asdasd", memberData?.memberName.toString(),)

                    // 예시: 글로벌 액션을 사용하여 PaymentFragment로 네비게이트
                    val action = ProductOrderBottomSheetFragmentDirections.actionGlobalPaymentFragment(
                        user_id,
                        user_type,
                        memberData?.memberName.toString(),
                        memberData?.memberPhoneNumber.toString(),
                        "서울시 강남구 역삼동",
                        "order",
                        productData
                    )
                    findNavController().navigate(action)

                }
                // TODO: 주문 처리 로직 구현
                // dismiss()
            }

            // 수량 증가 버튼
            btnIncrease.setOnClickListener {
                viewModel.updateQuantity(true)
            }

            // 수량 감소 버튼
            btnDecrease.setOnClickListener {
                viewModel.updateQuantity(false)
            }
        }
    }

    // LiveData 관찰
    private fun observeViewModel() {
        // 재고 수량 관찰
        viewModel.quantity.observe(viewLifecycleOwner) { quantity ->
            binding.tvQuantity.text = "$quantity"
        }

        // 선택 컬러 관찰
        viewModel.selectedColor.observe(viewLifecycleOwner) { color ->
            selectedColor = color
        }
    }

    // 장바구니 화면 이동 확인 Dialog
    private fun showActionDialog() {
        CustomDialog(
            context = requireContext(),
            message = "장바구니에 상품이 담겼습니다.\n장바구니로 이동하시겠습니까?",
            confirmText = "장바구니로 이동",
            cancelText = "쇼핑 계속하기",
            onConfirm = {
                dismiss() // BottomSheet 닫기
                cartClickListener?.onMoveToCartAction()
            },
            onCancel = {
                // 현재 화면 유지
            }
        ).show()
    }

    fun setOnCartClickListener(listener: OnCartClickListener) {
        cartClickListener = listener
    }

    companion object {
        private const val PRODUCT_KEY = "PRODUCT_KEY"

        fun newInstance(product: Product): ProductOrderBottomSheetFragment {
            return ProductOrderBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PRODUCT_KEY, product)
                }
            }
        }
    }

}

interface OnCartClickListener {
    fun onMoveToCartAction()
}
