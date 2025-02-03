package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.Promotion
import com.nemodream.bangkkujaengi.customer.data.model.SortType
import com.nemodream.bangkkujaengi.customer.data.model.SubCategoryType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromotionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {
    suspend fun getPromotionByTitle(
        title: String,
        sortType: SortType,
        startAfter: DocumentSnapshot? = null,
        userId: String,
    ): List<Product> {
        try {
            // 1. 프로모션의 productIds 가져오기
            val promotionSnapshot = firestore.collection("Promotion")
                .whereEqualTo("title", title)
                .get()
                .await()
                .documents.first()

            val promotion = promotionSnapshot.toObject<Promotion>() ?: Promotion()
            if (promotion.productIds.isEmpty()) return emptyList()

            // 2. 해당 프로모션의 상품들 가져오기
            var query = firestore.collection("Product")
                .whereIn(FieldPath.documentId(), promotion.productIds)
                .let { baseQuery ->
                    when (sortType) {
                        SortType.PURCHASE -> baseQuery.orderBy(
                            "purchaseCount",
                            Query.Direction.DESCENDING
                        )

                        SortType.REVIEW -> baseQuery.orderBy(
                            "reviewCount",
                            Query.Direction.DESCENDING
                        )

                        SortType.PRICE_HIGH -> baseQuery.orderBy(
                            "price",
                            Query.Direction.DESCENDING
                        )

                        SortType.PRICE_LOW -> baseQuery.orderBy("price", Query.Direction.ASCENDING)
                        SortType.VIEWS -> baseQuery.orderBy("viewCount", Query.Direction.DESCENDING)
                        SortType.DISCOUNT -> baseQuery.orderBy(
                            "saleRate",
                            Query.Direction.DESCENDING
                        )

                        SortType.LATEST -> baseQuery.orderBy(
                            "createdAt",
                            Query.Direction.DESCENDING
                        )
                    }
                }

            if (startAfter != null) {
                query = query.startAfter(startAfter)
            }

            // 3. 프로모션 필터 적용
            val products = query.get().await().documents.map { doc ->
                doc.toObject<Product>()?.copy(
                    productId = doc.id,
                    images = (doc.get("images") as? List<String>)?.map { imagePath ->
                        getImageUrl(imagePath)
                    } ?: emptyList(),
                    like = isProductLiked(userId, doc.id)
                ) ?: Product()
            }

            val filteredProducts =
                if (promotion.filterField != null && promotion.filterValue != null) {
                    products.filter {
                        when (promotion.filterType) {
                            "GREATER_THAN" -> it.getFieldValue(promotion.filterField) > promotion.filterValue
                            else -> true
                        }
                    }
                } else products

            return filteredProducts.take(10)

        } catch (e: Exception) {
            Log.e("PromotionRepository", "Error fetching promotion data", e)
            throw e
        }
    }

    private fun Product.getFieldValue(fieldName: String): Int {
        return when (fieldName) {
            "purchaseCount" -> purchaseCount
            "reviewCount" -> reviewCount
            "price" -> price
            "viewCount" -> viewCount
            "saleRate" -> saleRate
            else -> 0
        }
    }

    /*
    * Firebase Storage에서 상대 경로에 맞는 이미지 가져오기
    * */
    private suspend fun getImageUrl(imagePath: String) = storage.reference
        .child(imagePath)
        .downloadUrl
        .await()
        .toString()

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
}