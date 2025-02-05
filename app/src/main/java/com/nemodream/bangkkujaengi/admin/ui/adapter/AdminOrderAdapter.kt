package com.nemodream.bangkkujaengi.admin.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.databinding.RowPaymentCompletedBinding

class AdminOrderAdapter(
    private var orders: List<Order>,
    private val viewType: OrderViewType,
    private val onActionClick: (Order) -> Unit,
    private val onCancelClick: (Order) -> Unit,
    private val isOrderSelected: (String) -> Boolean,         // documentId 사용
    private val onOrderCheckedChange: (String, Boolean) -> Unit // documentId 사용
) : RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: RowPaymentCompletedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = RowPaymentCompletedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        // 디버깅 로그 추가
        Log.d("BindViewHolder", "Binding order: ${order.documentId}, Checkbox should be: ${isOrderSelected(order.documentId)}")

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

            // 체크박스 상태 설정
            cbRowPcSelect.isEnabled = isCheckboxEnabled
            cbRowPcSelect.isChecked = if (isCheckboxEnabled) {
                isOrderSelected(order.documentId)
            } else {
                false
            }

            // 체크박스 리스너 설정
            cbRowPcSelect.setOnCheckedChangeListener { _, isChecked ->
                onOrderCheckedChange(order.documentId, isChecked)
                Log.d("BindViewHolder", "Checkbox changed: $isChecked for order ${order.documentId}")
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
