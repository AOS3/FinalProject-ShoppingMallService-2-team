package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import com.nemodream.bangkkujaengi.customer.data.model.PaymentProduct
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import kotlinx.coroutines.tasks.await

class PaymentRepository {

    companion object {

        // 장바구니에서 체크 표시 된 상품 데이터만 가져오는 메소드
        suspend fun getting_payment_product_by_checked(user_id: String): MutableList<Map<String, *>> {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Cart")
            val result = collectionReference.whereEqualTo("userId", user_id).get().await()

            val checked_cart_item_list = mutableListOf<Map<String, *>>()

            result.forEach { document ->
                val cartData = document.toObject(PaymentProduct::class.java)

                // checked가 true인 항목만 필터링
                val filteredItems = cartData.items.filter { it.checked }

                // 필터링된 항목이 있다면 결과 리스트에 추가
                if (filteredItems.isNotEmpty()) {
                    val filteredCartData = PaymentProduct(
                        items = filteredItems,  // 필터링된 항목만 포함
                        userId = cartData.userId
                    )
                    val map = mapOf(
                        "cart_document_id" to document.id,
                        "checked_cart_data" to filteredCartData // 필터링된 데이터만 추가
                    )
                    checked_cart_item_list.add(map)
                }
            }

            // 디버깅용 로그 추가
            checked_cart_item_list.forEach { map ->
                val cartData = map["checked_cart_data"] as? PaymentProduct
                cartData?.items?.forEach { item ->
                    Log.d("test1010", "Checked Product ID: ${item.productId}, Quantity: ${item.quantity}")
                }
            }

            return checked_cart_item_list
        }

        // 상품 문서 id 리스트로 상품 가져오는 메소드
        suspend fun getting_prodcut_by_product_document_id(product_document_id_list: List<String>): MutableList<Map<String, *>> {
            Log.d("test200", "test2: ${product_document_id_list}")
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Product")

            // 결과를 저장할 리스트
            val product_list = mutableListOf<Map<String, *>>()

            product_document_id_list.forEach {
                val result = collectionReference.document(it).get().await()
                val map = mapOf(
                    "product_document_id" to it,
                    "product_data" to result.toObject(Product::class.java)
                )
                product_list.add(map)
            }

            product_list.forEach {
                Log.d("test200", "getting_prodcut_by_product_document_id: ${it["product_document_id"]}")
                Log.d("test200", "getting_prodcut_by_product_document_id: ${it["product_data"]}")
            }

            return product_list
        }


        // 유저 id를 통해 유저가 보유한 쿠폰 가져오는 메소드
        suspend fun getting_coupon_document_id_by_user_id(user_id: String): List<String> {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Member")

            return try {
                // Firestore에서 userId로 문서 검색
                val result = collectionReference.whereEqualTo("memberId", user_id).get().await()

                // couponDocumentId 필드 추출
                val couponDocumentIds = mutableListOf<String>()

                for (document in result.documents) {
                    // couponDocumentId 필드를 가져옴
                    val couponIds = document.get("couponDocumentId") as? List<*>

                    // String으로 변환 가능한 값만 필터링하여 추가
                    couponIds?.filterIsInstance<String>()?.let {
                        couponDocumentIds.addAll(it)
                    }
                }
                Log.d("FirestoreError2", "couponDocumentIds: $couponDocumentIds")
                // 결과 반환
                couponDocumentIds
            } catch (e: Exception) {
                // 예외 발생 시 로그 출력 및 빈 리스트 반환
                Log.e("FirestoreError2", "Error fetching couponDocumentId: ${e.message}")
                emptyList()
            }
        }


        // 유저가 가지고있는 쿠폰의 정보를 가져오는 메소드
        suspend fun getting_coupon_all(coupon_document_id_list: List<String>): MutableList<Coupon> {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Coupon")
            Log.d("coupon", "coupon_document_id_list: $coupon_document_id_list")
            val couponList = mutableListOf<Coupon>()

            try {
                val result = collectionReference.whereEqualTo("activity", true).get().await()
                Log.d("coupon", "Fetched documents: ${result.documents.size}")

                for (document in result.documents) {
                    val couponData = document.toObject(Coupon::class.java)?.copy(documentId = document.id)
                    if (couponData != null && coupon_document_id_list.contains(document.id)) {
                        couponList.add(couponData)
                    }
                }

                Log.d("coupon", "Final couponList: $couponList")
            } catch (e: Exception) {
                Log.e("coupon", "Error fetching coupons: ${e.message}", e)
            }

            return couponList
        }

        // 상품 document id로 상품 정보 가져오기
        suspend fun getting_coupon_by_select_coupon_document_id(couponDocumentIdList: List<String>): MutableList<Map<String, *>> {
            Log.d("test200", "test2: ${couponDocumentIdList}")
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Coupon")

            // 결과를 저장할 리스트
            val coupon_list = mutableListOf<Map<String, *>>()

            couponDocumentIdList.forEach {
                val result = collectionReference.document(it).get().await()
                val map = mapOf(
                    "coupon_document_id" to it,
                    "coupon_data" to result.toObject(Coupon::class.java)
                )
                coupon_list.add(map)
            }

            coupon_list.forEach {
                Log.d("test200", "getting_prodcut_by_product_document_id: ${it["product_document_id"]}")
                Log.d("test200", "getting_prodcut_by_product_document_id: ${it["product_data"]}")
            }

            return coupon_list
        }

        // 구매 항목 목록 id로 장바구니 항목 지우기
        suspend fun remove_cart_item_by_payment_product_document_id_list(user_id: String, payment_product_id_list: List<String>) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Cart")

            try {
                // 유저 ID로 Cart 문서 가져오기
                val result = collectionReference.whereEqualTo("userId", user_id).get().await()

                // 유저의 장바구니 데이터 처리
                for (document in result.documents) {
                    val cartData = document.toObject(PaymentProduct::class.java)

                    if (cartData != null) {
                        // payment_product_id_list에 해당하는 항목만 제거
                        val updatedItems = cartData.items.filterNot { payment_product_id_list.contains(it.productId) }

                        // 업데이트할 데이터 생성
                        val updatedCartData = mapOf("items" to updatedItems)

                        // Firestore 문서 업데이트
                        collectionReference.document(document.id).update(updatedCartData).await()

                        Log.d("CartUpdate", "Updated cart for documentId: ${document.id}")
                    }
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error removing cart items: ${e.message}", e)
            }
        }


        // 구매 항목 저장 메서드
        suspend fun add_purchase_product(purchase_product: MutableList<Purchase>) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Purchase")

            try {
                // Firestore에 구매 항목 저장
                purchase_product.forEach { purchase ->
                    collectionReference.add(purchase).await()
                    Log.d("PurchaseSave", "Added purchase: $purchase")
                }
            } catch (e: Exception) {
                // 에러 처리
                Log.e("FirestoreError", "Error adding purchase product: ${e.message}", e)
            }
        }

        // 유저 id로 유저 아이디 찾기
        suspend fun found_user_id_by_user_id(user_id: String): Boolean {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Member")

            return try {
                // Firestore에서 memberId가 user_id와 일치하는 문서 검색
                val result = collectionReference.whereEqualTo("memberId", user_id).get().await()
                Log.d("FirestoreCheck", "Found matching memberId: ${result.documents.size > 0}")

                // 결과가 존재하면 true, 없으면 false 반환
                result.documents.isNotEmpty()
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error checking user_id: ${e.message}", e)
                false
            }
        }

        // 구매시 수량만큼 상품 재고 감소, 구매 수 증가 메서드
        suspend fun update_product_count_purchase_count(productDocumentId: String, productCount: Int) {
            val firestore = FirebaseFirestore.getInstance()
            try {
                firestore.collection("Product")
                    .document(productDocumentId)
                    .update(
                        "productCount", FieldValue.increment(-productCount.toLong()),
                        "purchaseCount", FieldValue.increment(productCount.toLong())
                    ).await()
                Log.d("UpdateProduct", "Updated product $productDocumentId: decreased productCount by $productCount")
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error updating product counts: ${e.message}", e)
            }
        }

    }


}