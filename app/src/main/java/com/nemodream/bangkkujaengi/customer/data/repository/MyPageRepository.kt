package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Product
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {

    // 현재 로그인된 사용자 정보를 받아온다.
    suspend fun getMemberInfo(memberDocId: String): Member {
        // 현재 로그인된 사용자 정보를 받아온다.
        return firestore.collection("Member")
            .document(memberDocId)
            .get()
            .await()
            .toObject(Member::class.java)!!
    }

    suspend fun loadRecentProduct(userId: String): List<Product> {
        return try {
            val snapshot = firestore.collection("RecentProduct")
                .document(userId)
                .get()
                .await()

            val productIds = snapshot.get("productIds") as? List<String> ?: return emptyList()
            val products = productIds.mapNotNull { productId ->
                firestore.collection("Product")
                    .document(productId)
                    .get()
                    .await()
                    .toObject(Product::class.java)
                    ?.copy(like = isProductLiked(userId, productId))
            }

            products.subList(0, minOf(products.size, 7)).reversed()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getImageUrl(imagePath: String) = storage.reference
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

    // 닉네임 수정
    suspend fun updateNickname(userId: String, updatedNickname: String): Result<Unit> {
        return try {
            val existingUser = firestore.collection("Member")
                .whereEqualTo("memberNickName", updatedNickname)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                return Result.failure(Exception("이미 사용 중인 닉네임입니다."))
            }

            firestore.collection("Member")
                .document(userId)
                .update("memberNickName", updatedNickname)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}