package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import kotlinx.coroutines.tasks.await

class NonMemberOrderCheckRepository {

    companion object {

        suspend fun getting_non_member_order_check_data(
            purchaseId: String,
            nonMemberPassword: String
        ): List<Purchase> {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Purchase")

            return try {
                // Firestore에서 purchaseId와 nonMemberPassword가 일치하는 문서 검색
                val result = collectionReference
                    .whereEqualTo("purchaseId", purchaseId)
                    .whereEqualTo("nonMemberPassword", nonMemberPassword)
                    .get()
                    .await()

                // 검색된 문서를 Purchase 객체 리스트로 변환
                val purchaseList = result.documents.mapNotNull { document ->
                    document.toObject(Purchase::class.java)?.copy(documentId = document.id)
                }

                Log.d("FirestoreCheck", "Found ${purchaseList.size} matching orders.")
                purchaseList
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching non-member order data: ${e.message}", e)
                emptyList()
            }
        }

        suspend fun getting_non_member_order_check_data_by_phone_num(
            receiverPhone: String,
            nonMemberPassword: String
        ): List<Purchase> {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Purchase")

            return try {
                // 1️⃣ 먼저 receiverPhone과 nonMemberPassword가 일치하는 모든 데이터를 가져옴
                val result = collectionReference
                    .whereEqualTo("receiverPhone", receiverPhone)
                    .whereEqualTo("nonMemberPassword", nonMemberPassword)
                    .get()
                    .await()

                // 2️⃣ 가져온 데이터 중 가장 최신 purchaseDateTime 찾기
                val allPurchases = result.documents.mapNotNull { document ->
                    document.toObject(Purchase::class.java)?.copy(documentId = document.id)
                }

                if (allPurchases.isEmpty()) {
                    Log.d("FirestoreCheck", "No matching orders found.")
                    return emptyList()
                }

                val latestPurchaseDateTime = allPurchases.maxByOrNull { it.purchaseDateTime }?.purchaseDateTime
                    ?: return emptyList()

                Log.d("FirestoreCheck", "Latest purchaseDateTime: $latestPurchaseDateTime")

                // 3️⃣ 해당 purchaseDateTime과 같은 데이터들 가져오기
                val latestPurchases = collectionReference
                    .whereEqualTo("purchaseDateTime", latestPurchaseDateTime)
                    .get()
                    .await()

                val latestPurchaseList = latestPurchases.documents.mapNotNull { document ->
                    document.toObject(Purchase::class.java)?.copy(documentId = document.id)
                }

                Log.d("FirestoreCheck", "Found ${latestPurchaseList.size} orders with latest purchaseDateTime.")
                return latestPurchaseList
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching latest non-member order data: ${e.message}", e)
                emptyList()
            }
        }
    }
}