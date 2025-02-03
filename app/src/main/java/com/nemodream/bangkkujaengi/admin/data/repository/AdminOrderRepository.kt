package com.nemodream.bangkkujaengi.admin.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.admin.data.model.OrderState
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import kotlinx.coroutines.tasks.await

class AdminOrderRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun fetchOrdersByState(state: String): List<Order> {
        return try {
            val snapshot = firestore.collection("Purchase")
                .whereEqualTo("purchaseState", state)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    Order(
                        orderDate = data["purchaseDateTime"] as? String ?: data["purchaseDate"].toString(),
                        productName = data["productTitle"] as? String ?: "알 수 없음",
                        customerId = data["memberId"] as? String ?: "알 수 없음",
                        orderNumber = data["purchaseInvoiceNumber"].toString(),
                        deliveryStatus = data["deliveryStatus"] as? String,
                        invoiceNumber = data["invoiceNumber"].toString(),
                        deliveryStartDate = data["deliveryStartDate"] as? String ?: "알 수 없음",
                        deliveryDate = data["deliveryDate"] as? String ?: "알 수 없음",
                        cancelDate = data["cancelDate"] as? String ?: "알 수 없음",
                        canceledBy = data["canceledBy"] as? String ?: "알 수 없음",
                        cancellationReason = data["cancellationReason"] as? String ?: "알 수 없음",
                        state = mapPurchaseStateToOrderState(data["purchaseState"] as? String ?: "결제 완료")
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // 실패 시 빈 리스트 반환
        }
    }


    private fun mapPurchaseStateToOrderState(purchaseState: String): OrderState {
        return when (purchaseState) {
            "결제 완료" -> OrderState.PAYMENT_COMPLETED
            "상품 준비" -> OrderState.PRODUCT_READY
            "배송 중" -> OrderState.SHIPPING
            "구매 확정" -> OrderState.PURCHASE_CONFIRMED
            "취소됨" -> OrderState.CANCELED
            else -> OrderState.PAYMENT_COMPLETED // 기본 상태
        }
    }

    suspend fun updateOrderState(orderNumber: String, newState: String) {
        try {
            val query = firestore.collection("Purchase")
                .whereEqualTo("purchaseInvoiceNumber", orderNumber.toLong())
                .get()
                .await()

            if (query.documents.isNotEmpty()) {
                val document = query.documents.first()
                firestore.collection("Purchase").document(document.id)
                    .update("purchaseState", newState)
                    .await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("주문 상태를 업데이트하는 데 실패했습니다.")
        }
    }

    suspend fun updateOrderCancellation(
        orderNumber: String,
        cancelDate: String,
        canceledBy: String,
        cancellationReason: String
    ) {
        try {
            val query = firestore.collection("Purchase")
                .whereEqualTo("purchaseInvoiceNumber", orderNumber.toLong())
                .get()
                .await()

            if (query.documents.isNotEmpty()) {
                val document = query.documents.first()
                firestore.collection("Purchase").document(document.id).update(
                    mapOf(
                        "purchaseState" to OrderState.CANCELED,
                        "cancelDate" to cancelDate,
                        "canceledBy" to canceledBy,
                        "cancellationReason" to cancellationReason
                    )
                ).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("취소 상태 업데이트에 실패했습니다.")
        }
    }

    suspend fun updateOrderShippingDetails(
        orderNumber: String,
        deliveryStatus: String,
        deliveryStartDate: String,
        invoiceNumber: String,
        deliveryDate: String
    ) {
        try {
            val query = firestore.collection("Purchase")
                .whereEqualTo("purchaseInvoiceNumber", orderNumber.toLong())
                .get()
                .await()

            if (query.documents.isNotEmpty()) {
                val document = query.documents.first()
                firestore.collection("Purchase").document(document.id).update(
                    mapOf(
                        "purchaseState" to OrderState.SHIPPING.name,
                        "deliveryStatus" to deliveryStatus,
                        "deliveryStartDate" to deliveryStartDate,
                        "invoiceNumber" to invoiceNumber,
                        "deliveryDate" to deliveryDate
                    )
                ).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("배송 상태 업데이트에 실패했습니다.")
        }
    }
}