package com.nemodream.bangkkujaengi.admin.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.admin.data.model.OrderState
import com.nemodream.bangkkujaengi.admin.ui.adapter.AdminOrderAdapter
import com.nemodream.bangkkujaengi.admin.ui.adapter.OrderViewType
import com.nemodream.bangkkujaengi.admin.ui.custom.CustomCanceledDialog
import com.nemodream.bangkkujaengi.admin.ui.viewmodel.AdminOrderViewModel
import com.nemodream.bangkkujaengi.customer.ui.custom.CustomDialog

abstract class BaseAdminOrderFragment : Fragment() {

    protected abstract val viewType: OrderViewType
    protected abstract val orderState: OrderState
    protected val viewModel: AdminOrderViewModel by viewModels()

    protected lateinit var orderAdapter: AdminOrderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    // RecyclerView 초기화
    protected open fun setupRecyclerView(recyclerView: RecyclerView) {
        orderAdapter = AdminOrderAdapter(
            orders = emptyList(),
            viewType = viewType,
            onActionClick = { order -> handleNextState(order) },
            onCancelClick = { order -> handleCancel(order) },
            isOrderSelected = { orderNumber -> viewModel.isOrderSelected(orderNumber) },
            onOrderCheckedChange = { orderNumber, isChecked ->
                viewModel.updateOrderSelection(orderNumber, isChecked)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = orderAdapter
    }

    // ViewModel의 LiveData 관찰 설정
    private fun observeViewModel() {
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            orderAdapter.updateOrders(orders)
        }

        viewModel.headerCheckboxState.observe(viewLifecycleOwner) { state ->
            // 헤더 체크박스 상태 변경 시 UI 업데이트
            val headerCheckbox = view?.findViewById<MaterialCheckBox>(R.id.cb_order_pc_header)
            headerCheckbox?.checkedState = state
        }

        viewModel.selectedItemCount.observe(viewLifecycleOwner) { count ->
            // 선택된 항목 수에 따라 헤더의 동작 버튼 업데이트
            val cancelSelectionTextView = view?.findViewById<TextView>(R.id.tv_order_pc_cancel_selection)
            val prepareSelectionTextView = view?.findViewById<TextView>(R.id.tv_order_pc_prepare_selection)

            if (cancelSelectionTextView != null && prepareSelectionTextView != null) {
                updateHeaderActions(count, cancelSelectionTextView, prepareSelectionTextView)
            }
        }
    }

    // 헤더 체크박스 설정
    protected open fun setupHeaderCheckbox(
        checkbox: MaterialCheckBox,
        recyclerView: RecyclerView
    ) {
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            // 헤더 체크박스 상태에 따라 ViewModel 업데이트 호출
            viewModel.toggleSelectAllOrders(isChecked)

            // 어댑터 강제 갱신
            orderAdapter.notifyDataSetChanged() // 이 코드가 없으면 뷰가 갱신되지 않을 수 있음
        }
    }

    // 헤더와 목록 스크롤 동기화
    protected open fun setupScrollSync(
        headerScrollView: HorizontalScrollView,
        recyclerScrollView: HorizontalScrollView
    ) {
        headerScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            recyclerScrollView.scrollTo(scrollX, 0)
        }

        recyclerScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            headerScrollView.scrollTo(scrollX, 0)
        }
    }

    // 헤더 액션 버튼 업데이트
    private fun updateHeaderActions(
        selectedCount: Int,
        cancelSelectionTextView: TextView,
        prepareSelectionTextView: TextView
    ) {
        val hasSelection = selectedCount > 0

        // 버튼 활성화/비활성화
        cancelSelectionTextView.isEnabled = hasSelection
        prepareSelectionTextView.isEnabled = hasSelection

        // 선택 취소 클릭 이벤트
        cancelSelectionTextView.setOnClickListener {
            if (hasSelection) {
                val reasons = listOf("재고 부족", "가격 오류", "주문 정보 오류", "결제 오류", "기타")
                CustomCanceledDialog(
                    context = requireContext(),
                    message = "$selectedCount 건의 상품을 취소하시겠습니까?",
                    reasons = reasons,
                    onConfirm = { selectedReason ->
                        // 선택된 주문들을 취소 상태로 변경
                        val selectedOrders = viewModel.getSelectedOrders()
                        selectedOrders.forEach { order ->
                            // 현재 탭 상태 전달하여 취소 후 목록에서 제거
                            viewModel.handleCancel(order, canceledBy = "관리자", cancellationReason = selectedReason, currentTabState = orderState)
                        }

                        // 체크박스 상태 초기화
                        resetCheckboxes()

                        Toast.makeText(requireContext(), "선택된 주문이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onCancel = {}
                ).show()
            }
        }


        // 선택 준비/배송 클릭 이벤트
        prepareSelectionTextView.setOnClickListener {
            if (hasSelection) {
                val action = when (viewType) {
                    OrderViewType.PAYMENT_COMPLETED -> "준비 상태로"
                    OrderViewType.PRODUCT_READY -> "배송 상태로"
                    else -> ""
                }

                showConfirmationDialog(
                    message = "$selectedCount 건의 상품을 $action 변경하시겠습니까?",
                    onConfirmAction = {
                        // 선택된 주문들을 상태에 따라 변경
                        val selectedOrders = viewModel.getSelectedOrders()

                        when (viewType) {
                            // 결제 완료 상태에서 준비 상태로 변경
                            OrderViewType.PAYMENT_COMPLETED -> {
                                selectedOrders.forEach {
                                    viewModel.updateOrderState(it, OrderState.PRODUCT_READY, orderState)
                                }
                            }

                            // 상품 준비 상태에서 배송 상태로 변경
                            OrderViewType.PRODUCT_READY -> {
                                selectedOrders.forEach {
                                    viewModel.updateOrderToShipping(it, orderState)
                                }
                            }

                            else -> return@showConfirmationDialog
                        }

                        // 체크박스 상태 초기화
                        resetCheckboxes()

                        Toast.makeText(requireContext(), "선택된 주문이 $action 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // 체크박스 상태 초기화
    private fun resetCheckboxes() {
        val headerCheckbox = view?.findViewById<MaterialCheckBox>(R.id.cb_order_pc_header)
        headerCheckbox?.isChecked = false

        // 선택된 아이템 목록 초기화
        viewModel.clearSelectedOrders()
        viewModel.updateCheckboxState()
    }

    // 헤더 텍스트 설정
    protected open fun setupHeaderText(
        orderDateHeader: String,
        deliveryStatusHeader: String? = null,
        invoiceNumberHeader: String? = null,
        deliveryDateHeader: String? = null
    ) {
        // 헤더 텍스트를 동적으로 설정
        view?.findViewById<TextView>(R.id.tv_order_pc_order_date_header)?.text =
            orderDateHeader

        view?.findViewById<TextView>(R.id.tv_order_pc_order_delivery_status)?.apply {
            text = deliveryStatusHeader
            visibility = if (deliveryStatusHeader != null) View.VISIBLE else View.GONE
        }

        view?.findViewById<TextView>(R.id.tv_order_pc_order_invoice_number)?.apply {
            text = invoiceNumberHeader
            visibility = if (invoiceNumberHeader != null) View.VISIBLE else View.GONE
        }

        view?.findViewById<TextView>(R.id.tv_order_pc_order_delivery_date)?.apply {
            text = deliveryDateHeader
            visibility = if (deliveryDateHeader != null) View.VISIBLE else View.GONE
        }
    }

    // 헤더 버튼 설정
    protected open fun setupHeaderTextAndActions(
        cancelSelectionTextView: TextView,
        prepareSelectionTextView: TextView,
        headerCheckbox: MaterialCheckBox
    ) {
        when (viewType) {
            OrderViewType.PAYMENT_COMPLETED -> {
                configureHeaderButtons(
                    cancelSelectionTextView = cancelSelectionTextView,
                    prepareSelectionTextView = prepareSelectionTextView,
                    headerCheckbox = headerCheckbox,
                    cancelVisible = true,
                    prepareVisible = true,
                    prepareText = "선택준비",
                    checkboxEnabled = true
                )
            }

            OrderViewType.PRODUCT_READY -> {
                configureHeaderButtons(
                    cancelSelectionTextView = cancelSelectionTextView,
                    prepareSelectionTextView = prepareSelectionTextView,
                    headerCheckbox = headerCheckbox,
                    cancelVisible = false,
                    prepareVisible = true,
                    prepareText = "선택배송",
                    checkboxEnabled = true
                )
            }

            OrderViewType.SHIPPING,
            OrderViewType.PURCHASE_CONFIRMED,
            OrderViewType.CANCELED -> {
                configureHeaderButtons(
                    cancelSelectionTextView = cancelSelectionTextView,
                    prepareSelectionTextView = prepareSelectionTextView,
                    headerCheckbox = headerCheckbox,
                    cancelVisible = false,
                    prepareVisible = false,
                    checkboxEnabled = false
                )
            }
        }
    }

    // 확인 다이얼로그 표시
    private fun showConfirmationDialog(message: String, onConfirmAction: () -> Unit) {
        CustomDialog(
            context = requireContext(),
            message = message,
            confirmText = "확인",
            cancelText = "취소",
            onConfirm = onConfirmAction,
            onCancel = {}
        ).show()
    }

    // 헤더 버튼 구성
    private fun configureHeaderButtons(
        cancelSelectionTextView: TextView,
        prepareSelectionTextView: TextView,
        headerCheckbox: MaterialCheckBox,
        cancelVisible: Boolean,
        prepareVisible: Boolean,
        prepareText: String? = null,
        checkboxEnabled: Boolean
    ) {
        cancelSelectionTextView.visibility = if (cancelVisible) View.VISIBLE else View.GONE
        prepareSelectionTextView.visibility = if (prepareVisible) View.VISIBLE else View.GONE
        prepareText?.let { prepareSelectionTextView.text = it }
        headerCheckbox.isEnabled = checkboxEnabled
    }

    // 주문 목록 새로고침
    open fun refreshOrders() {
        viewModel.loadOrders(orderState)
    }

    abstract fun handleNextState(order: Order)

    abstract fun handleCancel(order: Order)
}

