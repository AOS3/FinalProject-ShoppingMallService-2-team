package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseState
import kotlinx.coroutines.tasks.await

class OrderHistoryRepository {

    companion object {

        // 주문 내역 가져오기
        suspend fun getting_order_history_list(user_id: String): List<Purchase> {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Purchase")

            return try {
                // Firestore에서 memberId로 검색하고 purchaseDate로 정렬
                val querySnapshot = collectionReference
                    .whereEqualTo("memberId", user_id) // memberId로 필터링
                    .orderBy("purchaseDate", Query.Direction.DESCENDING) // purchaseDate 기준 정렬
                    .get()
                    .await()

                // 결과를 Purchase 객체 리스트로 변환
                val orderHistoryList = querySnapshot.documents.mapNotNull { document ->
                    val purchase = document.toObject(Purchase::class.java)
                    purchase?.copy(documentId = document.id)
                }

                orderHistoryList.forEach {
                    Log.d("OrderHistory", "Fetched order history: $it")
                }

                // Log.d("OrderHistory", "Fetched order history: $orderHistoryList")
                orderHistoryList
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching order history: ${e.message}", e)
                emptyList()
            }
        }

        // userId와 purchaseDate로 주문 내역 가져오는 메소드
        suspend fun getting_order_history_list_by_userId_purchaseDate(
            user_id: String,
            purchase_date: String
        ): List<Purchase> {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Purchase")

            return try {
                val querySnapshot = collectionReference
                    .whereEqualTo("memberId", user_id) // memberId로 필터링
                    .whereEqualTo("purchaseDateTime", purchase_date) // purchaseDateTime으로 필터링
                    .get()
                    .await()

                // 결과를 Purchase 객체 리스트로 변환
                val orderHistoryList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Purchase::class.java)
                }

                orderHistoryList.forEach {
                    Log.d("OrderHistory2", "Filtered order history: $it")
                }

                orderHistoryList
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching filtered order history: ${e.message}", e)
                emptyList()
            }
        }

        suspend fun getting_purchase_data_by_document_id(document_id: String): Purchase? {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Purchase")
            return try {
                val documentSnapshot = collectionReference.document(document_id).get().await()
                Log.d("getting_purchase_data_by_document_id", "${documentSnapshot.toObject(Purchase::class.java)}")
                documentSnapshot.toObject(Purchase::class.java)
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching purchase data: ${e.message}", e)
                null
            }
        }

        suspend fun update_purchase_state(document_id: String) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Purchase")
            try {
                // Firestore에서 해당 documentId의 purchaseState를 업데이트
                collectionReference.document(document_id)
                    .update(
                        "purchaseState", PurchaseState.PURCHASE_CONFIRMED.name,
                        "purchaseConfirmedTime", Timestamp.now()
                    )
                    .await()

                Log.d("FirestoreUpdate", "Successfully updated purchaseState to PURCHASE_CONFIRMED for documentId: $document_id")
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error updating purchaseState: ${e.message}", e)
            }
        }



    }

}