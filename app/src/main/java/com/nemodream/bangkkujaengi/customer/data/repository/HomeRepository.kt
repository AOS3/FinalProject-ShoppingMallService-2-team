package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.Banner
import com.nemodream.bangkkujaengi.customer.data.model.Cart
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.Promotion
import com.nemodream.bangkkujaengi.customer.data.model.PromotionHeader
import com.nemodream.bangkkujaengi.customer.data.model.PromotionItem
import com.nemodream.bangkkujaengi.customer.data.model.PromotionProducts
import com.nemodream.bangkkujaengi.customer.data.model.SubCategoryType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {
    /*
    * 배너 데이터 가져오기
    * */
    suspend fun getBanners(): List<Banner> =
        firestore.collection("Banner")
            .get()
            .await()
            .documents
            .map { doc ->
                val banner = Banner(
                    id = doc.id,
                    productId = doc.getString("productId") ?: "",
                    thumbnailImageRef = doc.getString("imageUrl") ?: ""
                )
                banner.copy(thumbnailImageRef = getImageUrl(banner.thumbnailImageRef))
            }

    /*
    * Firebase Storage에서 상대 경로에 맞는 이미지 가져오기
    * */
    private suspend fun getImageUrl(imagePath: String) = storage.reference
        .child(imagePath)
        .downloadUrl
        .await()
        .toString()

    /*
    * productId를 통해 Firebase의 Product Collection에서 해당 상품 데이터 가져오기
    * */
    suspend fun getProducts(productId: String, userId: String): Product {
        val doc = firestore.collection("Product")
            .document(productId)
            .get()
            .await()

        // 문서가 존재하고 삭제되지 않은 경우에만 처리
        return if (doc.exists() && doc.getBoolean("delete") == false) {
            doc.toObject<Product>()?.copy(
                productId = doc.id,
                images = (doc.get("images") as? List<String>)?.map { imagePath ->
                    getImageUrl(imagePath)
                } ?: emptyList(),
                like = isProductLiked(userId, productId)
            ) ?: Product()
        } else {
            Product() // 삭제된 상품이거나 존재하지 않는 경우 빈 Product 객체 반환
        }
    }

    /*
    * 프로모션 섹션 데이터 가져오기
    * */
    suspend fun getPromotionSections(memberId: String): List<PromotionItem> {
        val sections = mutableListOf<PromotionItem>()

        try {
            val promotions = firestore.collection("Promotion")
                .orderBy("order")
                .get()
                .await()

            promotions.documents.forEach { doc ->
                val promotion = doc.toObject<Promotion>() ?: Promotion()

                if (promotion.isActive) {
                    sections.add(PromotionHeader(promotion.title))
                    // 병렬로 상품 정보 가져오기
                    val products = coroutineScope {
                        promotion.productIds.map { productId ->
                            async {
                                try {
                                    getProducts(productId, memberId)
                                } catch (e: Exception) {
                                    Log.e("PromotionRepository", "Error fetching product $productId: ", e)
                                    null
                                }
                            }
                        }.awaitAll().filterNotNull()
                    }
                    sections.add(PromotionProducts(products))
                }
            }
        } catch (e: Exception) {
            Log.e("PromotionRepository", "getPromotionSections error: ", e)
        }

        return sections
    }

    fun saveCartProduct(productId: String, quantity: Int = 1, color: String) {
        val userCartRef = firestore.collection("Cart").document("testuser")

        userCartRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Map으로 직접 데이터 처리
                    val items = (document.get("items") as? List<Map<String, Any>>)?.map {
                        Cart(
                            productId = it["productId"] as String,
                            quantity = (it["quantity"] as Long).toInt(),
                            color = it["color"] as String,
                        )
                    } ?: listOf()

                    val existingItem = items.find { it.productId == productId }

                    val updatedItems = if (existingItem != null) {
                        val newQuantity = existingItem.quantity + quantity
                        if (newQuantity > 10) {
                            return@addOnSuccessListener
                        }

                        items.map {
                            if (it.productId == productId) Cart(productId, newQuantity, color)
                            else it
                        }
                    } else {
                        items + Cart(productId, quantity, color)
                    }

                    // Map으로 변환하여 저장
                    val itemsToSave = updatedItems.map {
                        mapOf(
                            "productId" to it.productId,
                            "quantity" to it.quantity,
                            "color" to it.color,
                        )
                    }

                    userCartRef.update("items", itemsToSave)
                } else {
                    // 새 장바구니 생성
                    val cartData = mapOf(
                        "userId" to "testuser",
                        "items" to listOf(
                            mapOf(
                                "productId" to productId,
                                "quantity" to quantity,
                                "color" to color,
                            )
                        ),
                        "isDelete" to false
                    )
                    userCartRef.set(cartData)
                }
            }
    }

    /* 좋아요 토글 */
    suspend fun toggleProductLikeState(userId: String, productId: String) {
        try {
            firestore.runTransaction { transaction ->
                // 현재 좋아요 상태만 확인
                val likeDoc = transaction.get(
                    firestore.collection("ProductLike").document(userId)
                )

                val currentLikes = if (likeDoc.exists()) {
                    (likeDoc.get("productIds") as? List<String>) ?: emptyList()
                } else {
                    emptyList()
                }

                // 좋아요 상태 토글
                if (productId in currentLikes) {
                    // 좋아요 취소
                    transaction.update(
                        firestore.collection("ProductLike").document(userId),
                        mapOf(
                            "productIds" to currentLikes - productId,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    transaction.update(
                        firestore.collection("Product").document(productId),
                        "likeCount", FieldValue.increment(-1)
                    )
                } else {
                    // 좋아요 추가
                    transaction.set(
                        firestore.collection("ProductLike").document(userId),
                        mapOf(
                            "userId" to userId,
                            "productIds" to currentLikes + productId,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    transaction.update(
                        firestore.collection("Product").document(productId),
                        "likeCount", FieldValue.increment(1)
                    )
                }
            }.await()
        } catch (e: Exception) {
            Log.e("HomeRepository", "좋아요 상태변경 실패: ", e)
            throw e
        }
    }

    /* 상품 좋아요 여부 확인 */
    private suspend fun isProductLiked(userId: String, productId: String): Boolean {
        return try {
            val doc = firestore.collection("ProductLike")
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                val productIds = doc.get("productIds") as? List<String>
                productIds?.contains(productId) ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("HomeRepository", "좋아요 상태변경 실패: ", e)
            false
        }
    }

    // 최근 본 상품을 RecentProduct 컬렉션에 저장한다.
    fun saveRecentViewProduct(productId: String, userId: String) {
        val recentProductRef = firestore.collection("RecentProduct").document(userId)

        recentProductRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 기존 상품 목록을 가져옴
                    val productIds = (document.get("productIds") as? List<String>) ?: emptyList()

                    // 이미 있는 상품이 아닐 경우에만 추가
                    if (!productIds.contains(productId)) {
                        recentProductRef.update("productIds", productIds + productId)
                    }
                } else {
                    // 문서가 없는 경우 새로 생성
                    val data = hashMapOf(
                        "productIds" to listOf(productId),
                        "userId" to userId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    recentProductRef.set(data)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SaveRecentProduct", "Error saving recent product", e)
            }
    }
}