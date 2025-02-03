package com.nemodream.bangkkujaengi.admin.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.checkbox.MaterialCheckBox
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.admin.data.model.OrderState
import com.nemodream.bangkkujaengi.admin.data.repository.AdminOrderRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrderViewModel(
    private val repository: AdminOrderRepository = AdminOrderRepository()
) : ViewModel() {

    private val _orders = MutableLiveData<List<Order>>(emptyList())
    val orders: LiveData<List<Order>> get() = _orders

    private val selectedOrders = mutableSetOf<String>()  // documentId 기준으로 선택된 주문 관리

    private val _headerCheckboxState = MutableLiveData<Int>()
    val headerCheckboxState: LiveData<Int> get() = _headerCheckboxState

    private val _filteredOrders = MutableLiveData<List<Order>>()
    val filteredOrders: LiveData<List<Order>> get() = _filteredOrders

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _selectedItemCount = MutableLiveData<Int>()
    val selectedItemCount: LiveData<Int> get() = _selectedItemCount

    init {
        Log.d("ViewModelInit", "Initializing ViewModel")
        _selectedItemCount.value = 0
    }

    // 주문 목록 로드
    fun loadOrders(state: OrderState) {
        viewModelScope.launch {
            try {
                val result = repository.fetchOrdersByState(state.name)
                _orders.value = result.toList()
                _filteredOrders.value = result.toList()
            } catch (e: Exception) {
                _errorMessage.value = "주문 데이터를 가져오는 데 실패했습니다."
            }
        }
    }

    // 주문 취소 처리 (documentId 사용)
    fun handleCancel(order: Order, canceledBy: String, cancellationReason: String, currentTabState: OrderState) {
        viewModelScope.launch {
            try {
                val cancelDate = getCurrentTime()
                repository.updateOrderCancellation(order.documentId, cancelDate, canceledBy, cancellationReason)

                _orders.value = _orders.value?.filterNot {
                    it.documentId == order.documentId && currentTabState != OrderState.CANCELED
                }
            } catch (e: Exception) {
                _errorMessage.value = "취소 처리에 실패했습니다."
            }
        }
    }

    // 주문 선택 여부 확인
    fun isOrderSelected(documentId: String): Boolean {
        return selectedOrders.contains(documentId)
    }

    // 주문 선택 상태 업데이트
    fun updateOrderSelection(documentId: String, isChecked: Boolean) {
        if (isChecked) selectedOrders.add(documentId) else selectedOrders.remove(documentId)
        _selectedItemCount.value = selectedOrders.size
        updateCheckboxState()
    }

    fun toggleSelectAllOrders(selectAll: Boolean) {
        val currentOrders = _orders.value ?: return

        if (selectAll) {
            selectedOrders.addAll(currentOrders.map { it.documentId })
        } else {
            selectedOrders.clear()
        }

        _selectedItemCount.value = selectedOrders.size
        updateCheckboxState()
    }

    fun getSelectedOrders(): List<Order> {
        return _orders.value?.filter { selectedOrders.contains(it.documentId) } ?: emptyList()
    }

    // 주문 상태 업데이트 (documentId 사용)
    fun updateOrderState(order: Order, newState: OrderState, currentTabState: OrderState) {
        viewModelScope.launch {
            try {
                repository.updateOrderState(order.documentId, newState.name)

                _orders.value = _orders.value?.filterNot {
                    it.documentId == order.documentId && currentTabState != newState
                }
            } catch (e: Exception) {
                _errorMessage.value = "주문 상태를 업데이트하는 데 실패했습니다."
            }
        }
    }

    // 배송 상태 업데이트 (documentId 사용)
    fun updateOrderToShipping(order: Order, currentTabState: OrderState) {
        viewModelScope.launch {
            try {
                val deliveryStartDate = getCurrentTime()
                val invoiceNumber = generateInvoiceNumber()

                repository.updateOrderShippingDetails(
                    documentId = order.documentId,
                    deliveryStatus = "배송중",
                    deliveryStartDate = deliveryStartDate,
                    invoiceNumber = invoiceNumber,
                    deliveryDate = "--"
                )

                _orders.value = _orders.value?.filterNot {
                    it.documentId == order.documentId && currentTabState != OrderState.SHIPPING
                }
            } catch (e: Exception) {
                _errorMessage.value = "배송 상태 업데이트에 실패했습니다."
            }
        }
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun generateInvoiceNumber(): String {
        return "INV-${System.currentTimeMillis()}"
    }

    fun updateCheckboxState() {
        val currentOrders = _orders.value ?: return
        val selectedCount = selectedOrders.size
        val totalCount = currentOrders.size

        _headerCheckboxState.value = when {
            selectedCount == 0 -> MaterialCheckBox.STATE_UNCHECKED
            selectedCount == totalCount -> MaterialCheckBox.STATE_CHECKED
            else -> MaterialCheckBox.STATE_INDETERMINATE
        }
    }

    fun clearSelectedOrders() {
        selectedOrders.clear()
        _selectedItemCount.value = 0
    }
}
