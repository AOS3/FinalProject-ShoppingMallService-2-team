package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.Product
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyBookmarkRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {

    // 좋아요 상품만 가져오기
    suspend fun loadMyBookmarkList(userId: String): List<Product> {
        return try {
            // 1. 사용자의 좋아요 목록 가져오기
            val likeDoc = firestore.collection("ProductLike")
                .document(userId)
                .get()
                .await()

            // 2. 좋아요한 상품 ID 목록 추출
            val productIds = if (likeDoc.exists()) {
                likeDoc.get("productIds") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }

            if (productIds.isEmpty()) return emptyList()

            // 3. 각 상품 ID에 해당하는 상품 정보 가져오기
            productIds.mapNotNull { productId ->
                try {
                    getProducts(productId, userId)
                } catch (e: Exception) {
                    Log.e("Repository", "Error loading product $productId", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error loading bookmarks", e)
            emptyList()
        }
    }

    /*
* productId를 통해 Firebase의 Product Collection에서 해당 상품 데이터 가져오기
* */
    suspend fun getProducts(productId: String, userId: String): Product {  // userId 기본값 설정
        val doc = firestore.collection("Product")
            .document(productId)
            .get()
            .await()

        return doc.toObject<Product>()?.copy(
            productId = doc.id,
            images = (doc.get("images") as? List<String>)?.map { imagePath ->
                getImageUrl(imagePath)
            } ?: emptyList(),
            like = true,
        ) ?: Product()
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

}