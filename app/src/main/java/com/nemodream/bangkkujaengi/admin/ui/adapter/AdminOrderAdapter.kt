package com.nemodream.bangkkujaengi.admin.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.databinding.RowPaymentCompletedBinding

class AdminOrderAdapter(
    private var orders: List<Order>,          // 주문 데이터
    private val viewType: OrderViewType,      // 화면 타입에 따라 다르게 표시
    private val onActionClick: (Order) -> Unit, // 다음 상태 버튼 클릭 이벤트 처리
    private val onCancelClick: (Order) -> Unit, // 취소 버튼 클릭 이벤트 처리
    private val isOrderSelected: (String) -> Boolean,
    private val onOrderCheckedChange: (String, Boolean) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: RowPaymentCompletedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = RowPaymentCompletedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        with(holder.binding) {
            // 기존 리스너 제거 및 상태 초기화
            cbRowPcSelect.setOnCheckedChangeListener(null)

            // 체크박스 활성화 여부 설정
            val isCheckboxEnabled = when (viewType) {
                OrderViewType.SHIPPING,
                OrderViewType.PURCHASE_CONFIRMED,
                OrderViewType.CANCELED -> false
                else -> true
            }
            cbRowPcSelect.isEnabled = isCheckboxEnabled
            cbRowPcSelect.isChecked = if (isCheckboxEnabled) {
                order.orderNumber?.let { isOrderSelected(it) } ?: false
            } else {
                false
            }

            cbRowPcSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isCheckboxEnabled) {
                    order.orderNumber?.let { onOrderCheckedChange(it, isChecked) }
                }
            }

            // 필드 설정
            tvRowPcOrderDate.text = order.orderDate
            tvRowPcProductName.text = order.productName
            tvRowPcCustomerId.text = order.customerId
            tvRowPcOrderNumber.text = order.orderNumber

            // 탭(화면) 별 동적 UI 처리
            when (viewType) {
                OrderViewType.PAYMENT_COMPLETED -> {
                    btnRowPcNextState.visibility = View.VISIBLE
                    btnRowPcNextState.text = "상품 준비"
                    btnRowPcCancel.visibility = View.VISIBLE
                    tvRowPcDeliveryStatus.visibility = View.GONE
                    tvRowPcInvoiceNumber.visibility = View.GONE
                    tvRowPcDeliveryDate.visibility = View.GONE
                }

                OrderViewType.PRODUCT_READY -> {
                    btnRowPcNextState.visibility = View.VISIBLE
                    btnRowPcNextState.text = "배송"
                    btnRowPcCancel.visibility = View.GONE
                    tvRowPcDeliveryStatus.visibility = View.GONE
                    tvRowPcInvoiceNumber.visibility = View.GONE
                    tvRowPcDeliveryDate.visibility = View.GONE
                }

                OrderViewType.SHIPPING -> {
                    tvRowPcOrderDate.text = order.deliveryStartDate
                    tvRowPcDeliveryStatus.visibility = View.VISIBLE
                    tvRowPcDeliveryStatus.text = order.deliveryStatus
                    tvRowPcInvoiceNumber.visibility = View.VISIBLE
                    tvRowPcInvoiceNumber.text = order.invoiceNumber
                    tvRowPcDeliveryDate.visibility = View.VISIBLE
                    tvRowPcDeliveryDate.text = order.deliveryDate
                    btnRowPcNextState.visibility = View.GONE
                    btnRowPcCancel.visibility = View.GONE
                }

                OrderViewType.PURCHASE_CONFIRMED -> {
                    tvRowPcOrderDate.text = order.confirmationDate
                    btnRowPcNextState.visibility = View.GONE
                    btnRowPcCancel.visibility = View.GONE
                    tvRowPcDeliveryStatus.visibility = View.GONE
                    tvRowPcInvoiceNumber.visibility = View.GONE
                    tvRowPcDeliveryDate.visibility = View.GONE
                }

                OrderViewType.CANCELED -> {
                    tvRowPcOrderDate.text = order.cancelDate
                    tvRowPcDeliveryStatus.visibility = View.VISIBLE
                    tvRowPcDeliveryStatus.text = order.canceledBy
                    btnRowPcNextState.visibility = View.GONE
                    btnRowPcCancel.visibility = View.GONE
                    tvRowPcInvoiceNumber.visibility = View.VISIBLE
                    tvRowPcInvoiceNumber.text = order.cancellationReason
                    tvRowPcDeliveryDate.visibility = View.GONE

                }
            }

            // 버튼 클릭 이벤트 처리
            btnRowPcNextState.setOnClickListener { onActionClick(order) }
            btnRowPcCancel.setOnClickListener { onCancelClick(order) }
        }
    }

    fun updateOrders(newOrders: List<Order>) {
        val diffResult = DiffUtil.calculateDiff(AdminOrderDiffCallback(orders, newOrders))
        orders = newOrders
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = orders.size
}

// ViewType Enum
enum class OrderViewType {
    PAYMENT_COMPLETED,  // 결제 완료
    PRODUCT_READY,      // 상품 준비
    SHIPPING,           // 배송
    PURCHASE_CONFIRMED, // 구매 확정
    CANCELED            // 취소
}