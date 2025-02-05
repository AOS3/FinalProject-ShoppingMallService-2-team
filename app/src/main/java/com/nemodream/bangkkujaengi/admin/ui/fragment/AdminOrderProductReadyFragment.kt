package com.nemodream.bangkkujaengi.admin.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.admin.data.model.OrderState
import com.nemodream.bangkkujaengi.admin.ui.adapter.OrderViewType
import com.nemodream.bangkkujaengi.admin.ui.custom.CustomTextFieldDialog
import com.nemodream.bangkkujaengi.databinding.FragmentAdminOrderProductReadyBinding

// 상품 준비
class AdminOrderProductReadyFragment : BaseAdminOrderFragment() {
    private var _binding: FragmentAdminOrderProductReadyBinding? = null
    private val binding get() = _binding!!

    override val viewType = OrderViewType.PRODUCT_READY
    override val orderState = OrderState.PRODUCT_READY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrderProductReadyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeaderText(
            orderDateHeader = "주문일시"
        )
        setupRecyclerView(binding.recyclerViewProductReady)
        setupHeaderCheckbox(binding.layoutHeader.cbOrderPcHeader, binding.recyclerViewProductReady)
        setupScrollSync(headerScrollView = binding.layoutHeader.scrOrderPcHeader, recyclerScrollView = binding.scrollableRecyclerView)
        setupHeaderTextAndActions(
            cancelSelectionTextView = binding.layoutHeader.tvOrderPcCancelSelection,
            prepareSelectionTextView = binding.layoutHeader.tvOrderPcPrepareSelection,
            headerCheckbox = binding.layoutHeader.cbOrderPcHeader
        )
        viewModel.loadOrders(orderState)
    }

    override fun handleNextState(order: Order) {
        // 송장 번호 입력을 위한 다이얼로그 호출
        CustomTextFieldDialog(
            context = requireContext(),
            message = "송장 번호를 입력하세요.",
            hint = "송장 번호 입력",
            onConfirm = { inputText ->
                val invoiceNumber = inputText.trim()
                if (invoiceNumber.isEmpty()) {
                    Toast.makeText(requireContext(), "송장 번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    return@CustomTextFieldDialog
                }

                val deliveryStartDate = getCurrentTime()
                viewModel.updateOrderToShipping(
                    order = order,
                    deliveryStatus = "배송중",
                    deliveryStartDate = deliveryStartDate,
                    invoiceNumber = invoiceNumber
                )
                Toast.makeText(requireContext(), "배송 상태가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
            },
            onCancel = {}
        ).show()
    }

    override fun handleCancel(order: Order) {

    }

    // 현재 시간을 반환하는 메서드
    private fun getCurrentTime(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}
