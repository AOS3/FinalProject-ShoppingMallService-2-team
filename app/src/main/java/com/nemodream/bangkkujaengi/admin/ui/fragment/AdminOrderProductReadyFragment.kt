package com.nemodream.bangkkujaengi.admin.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.admin.data.model.OrderState
import com.nemodream.bangkkujaengi.admin.ui.adapter.OrderViewType
import com.nemodream.bangkkujaengi.customer.ui.custom.CustomDialog
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
        CustomDialog(
            context = requireContext(),
            message = "선택한 상품 \"${order.productName}\"을 배송하시겠습니까?",
            confirmText = "확인",
            cancelText = "취소",
            onConfirm = {
                viewModel.updateOrderToShipping(order, orderState)
            },
            onCancel = {}
        ).show()
    }

    override fun handleCancel(order: Order) {

    }
}
