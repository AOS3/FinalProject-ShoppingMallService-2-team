package com.nemodream.bangkkujaengi.admin.ui.viewmodel

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

    // 주문 목록 LiveData (RecyclerView와 연결됨)
    private val _orders = MutableLiveData<List<Order>>(emptyList())
    val orders: LiveData<List<Order>> get() = _orders

    // 선택된 주문 번호를 저장하는 Set
    private val selectedOrders = mutableSetOf<String>()

    // 헤더 체크박스 상태를 관리하는 LiveData
    private val _headerCheckboxState = MutableLiveData<Int>()
    val headerCheckboxState: LiveData<Int> get() = _headerCheckboxState

    // 필터링된 주문 목록
    private val _filteredOrders = MutableLiveData<List<Order>>()
    val filteredOrders: LiveData<List<Order>> get() = _filteredOrders

    // 에러 메시지를 관리하는 LiveData
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // 선택된 항목 수를 관리하는 LiveData
    private val _selectedItemCount = MutableLiveData<Int>()
    val selectedItemCount: LiveData<Int> get() = _selectedItemCount

    init {
        // 초기값 설정
        _selectedItemCount.value = 0
    }

    // 주문 목록 로드
    fun loadOrders(state: OrderState) {
        viewModelScope.launch {
            try {
                val result = repository.fetchOrdersByState(state.name)
                _orders.value = result.toList()  // 변화가 없더라도 LiveData 갱신 유도
                _filteredOrders.value = result.toList()
            } catch (e: Exception) {
                _errorMessage.value = "주문 데이터를 가져오는 데 실패했습니다."
            }
        }
    }

    // 주문 취소 처리
    fun handleCancel(order: Order, canceledBy: String, cancellationReason: String, currentTabState: OrderState) {
        viewModelScope.launch {
            try {
                val cancelDate = getCurrentTime()

                // Firestore 상태 업데이트
                repository.updateOrderCancellation(order.orderNumber, cancelDate, canceledBy, cancellationReason)

                // 상태가 변경된 항목을 현재 탭에서 제거
                _orders.value = _orders.value?.filterNot {
                    it.orderNumber == order.orderNumber && currentTabState != OrderState.CANCELED
                }
            } catch (e: Exception) {
                _errorMessage.value = "취소 처리에 실패했습니다."
            }
        }
    }

    // 주문 선택 여부 확인
    fun isOrderSelected(orderNumber: String): Boolean {
        return selectedOrders.contains(orderNumber)
    }

    // 주문 선택 상태 업데이트
    fun updateOrderSelection(orderNumber: String, isChecked: Boolean) {
        if (isChecked) selectedOrders.add(orderNumber) else selectedOrders.remove(orderNumber)
        _selectedItemCount.value = selectedOrders.size
        updateCheckboxState()
    }

    fun toggleSelectAllOrders(selectAll: Boolean) {
        val currentOrders = _orders.value ?: return
        selectedOrders.clear()

        if (selectAll) {
            currentOrders.forEach { selectedOrders.add(it.orderNumber) }
        }
        _selectedItemCount.value = selectedOrders.size
        updateCheckboxState()

        _orders.value = currentOrders.toList()
    }

    fun getSelectedOrders(): List<Order> {
        return _orders.value?.filter { selectedOrders.contains(it.orderNumber) } ?: emptyList()
    }

    fun updateOrderState(order: Order, newState: OrderState, currentTabState: OrderState) {
        viewModelScope.launch {
            try {
                // Firestore 상태 업데이트
                repository.updateOrderState(order.orderNumber, newState.name)

                // 상태가 변경된 항목을 현재 탭에서 제거
                _orders.value = _orders.value?.filterNot {
                    it.orderNumber == order.orderNumber && currentTabState != newState
                }
            } catch (e: Exception) {
                _errorMessage.value = "주문 상태를 업데이트하는 데 실패했습니다."
            }
        }
    }

    fun updateOrderToShipping(order: Order, currentTabState: OrderState) {
        viewModelScope.launch {
            try {
                val deliveryStartDate = getCurrentTime()  // 현재 시간 가져오기
                val invoiceNumber = generateInvoiceNumber()

                // Firestore 상태 업데이트
                repository.updateOrderShippingDetails(
                    orderNumber = order.orderNumber,
                    deliveryStatus = "배송중",
                    deliveryStartDate = deliveryStartDate,
                    invoiceNumber = invoiceNumber,
                    deliveryDate = "--"
                )

                // 상태가 변경된 항목을 현재 탭에서 제거
                _orders.value = _orders.value?.filterNot {
                    it.orderNumber == order.orderNumber && currentTabState != OrderState.SHIPPING
                }

            } catch (e: Exception) {
                _errorMessage.value = "배송 상태 업데이트에 실패했습니다."
            }
        }
    }

    // 현재 시간을 포맷팅하여 반환하는 함수
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun generateInvoiceNumber(): String {
        // 송장번호 생성 로직 (랜덤 값 또는 특정 규칙에 따라 생성)
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
