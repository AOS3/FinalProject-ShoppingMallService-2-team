package com.nemodream.bangkkujaengi.admin.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.admin.data.model.OrderState
import com.nemodream.bangkkujaengi.admin.ui.adapter.OrderViewType
import com.nemodream.bangkkujaengi.databinding.FragmentAdminOrderCanceledBinding

// 취소
class AdminOrderCanceledFragment : BaseAdminOrderFragment() {
    private var _binding: FragmentAdminOrderCanceledBinding? = null
    private val binding get() = _binding!!

    override val viewType = OrderViewType.CANCELED
    override val orderState = OrderState.CANCELED

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrderCanceledBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeaderText(
            orderDateHeader = "취소일시",
            deliveryStatusHeader = "취소측",
            invoiceNumberHeader = "사유"
        )
        setupRecyclerView(binding.recyclerViewCanceled)
        setupHeaderCheckbox(binding.layoutHeader.cbOrderPcHeader, binding.recyclerViewCanceled)
        setupScrollSync(headerScrollView = binding.layoutHeader.scrOrderPcHeader, recyclerScrollView = binding.scrollableRecyclerView)
        setupHeaderTextAndActions(
            cancelSelectionTextView = binding.layoutHeader.tvOrderPcCancelSelection,
            prepareSelectionTextView = binding.layoutHeader.tvOrderPcPrepareSelection,
            headerCheckbox = binding.layoutHeader.cbOrderPcHeader
        )
        viewModel.loadOrders(orderState)
    }

    override fun handleNextState(order: Order) {

    }

    override fun handleCancel(order: Order) {

    }
}

