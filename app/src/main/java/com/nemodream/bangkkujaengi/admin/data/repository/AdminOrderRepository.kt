package com.nemodream.bangkkujaengi.admin.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.admin.data.model.Order
import com.nemodream.bangkkujaengi.admin.data.model.OrderState
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
                    Log.d("FetchOrders", "문서 ID: ${doc.id}")

                    Order(
                        documentId = doc.id,
                        orderDate = data["purchaseDateTime"] as? String ?: data["purchaseDate"].toString(),
                        productName = data["productTitle"] as? String ?: "알 수 없음",
                        customerId = data["memberId"] as? String ?: "알 수 없음",
                        orderNumber = data["purchaseId"] as? String ?: "알 수 없음",
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
                    Log.w("FetchOrders", "문서 ID: ${doc.id}의 데이터가 null입니다.")
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun mapPurchaseStateToOrderState(purchaseState: String): OrderState {
        return when (purchaseState) {
            "결제 완료" -> OrderState.PAYMENT_COMPLETED
            "상품 준비" -> OrderState.PRODUCT_READY
            "배송 중" -> OrderState.SHIPPING
            "구매 확정" -> OrderState.PURCHASE_CONFIRMED
            "취소됨" -> OrderState.CANCELED
            else -> OrderState.PAYMENT_COMPLETED
        }
    }

    suspend fun updateOrderState(documentId: String, newState: String) {
        try {
            firestore.collection("Purchase").document(documentId)
                .update("purchaseState", newState)
                .await()
            Log.d("UpdateOrderState", "문서 ID: $documentId 상태가 $newState 로 업데이트되었습니다.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("주문 상태를 업데이트하는 데 실패했습니다.")
        }
    }

    suspend fun updateOrderCancellation(
        documentId: String,
        cancelDate: String,
        canceledBy: String,
        cancellationReason: String
    ) {
        try {
            firestore.collection("Purchase").document(documentId).update(
                mapOf(
                    "purchaseState" to OrderState.CANCELED.name,
                    "cancelDate" to cancelDate,
                    "canceledBy" to canceledBy,
                    "cancellationReason" to cancellationReason
                )
            ).await()
            Log.d("UpdateOrderCancellation", "문서 ID: $documentId 취소 상태로 업데이트되었습니다.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("취소 상태 업데이트에 실패했습니다.")
        }
    }

    suspend fun updateOrderShippingDetails(
        documentId: String,
        deliveryStatus: String,
        deliveryStartDate: String,
        invoiceNumber: String,
        deliveryDate: String
    ) {
        try {
            firestore.collection("Purchase").document(documentId).update(
                mapOf(
                    "purchaseState" to OrderState.SHIPPING.name,
                    "deliveryStatus" to deliveryStatus,
                    "deliveryStartDate" to deliveryStartDate,
                    "invoiceNumber" to invoiceNumber,
                    "deliveryDate" to deliveryDate
                )
            ).await()
            Log.d("UpdateOrderShippingDetails", "문서 ID: $documentId 배송 정보가 업데이트되었습니다.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("배송 상태 업데이트에 실패했습니다.")
        }
    }
}
