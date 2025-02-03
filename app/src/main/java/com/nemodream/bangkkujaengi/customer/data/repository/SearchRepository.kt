package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.local.dao.SearchHistoryDao
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SearchHistory
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {
    suspend fun getAllSearchHistory(): List<SearchHistory> =
        searchHistoryDao.getSearchHistoryList()

    suspend fun addSearch(query: String) {
        val existingSearch = searchHistoryDao.findSearchByQuery(query)
        if (existingSearch != null) {
            searchHistoryDao.updateSearch(existingSearch.copy(timestamp = System.currentTimeMillis()))
        } else {
            searchHistoryDao.insertSearch(
                SearchHistory(
                    query = query,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteSearch(search: SearchHistory) {
        searchHistoryDao.deleteSearch(search)
    }

    suspend fun deleteAllSearches() {
        searchHistoryDao.deleteAllSearches()
    }

    /*
    * firebase에서 productName이 포함되는 상품 정보를 가져온다.
    * */
    suspend fun getProductsByKeyword(keyword: String, userId: String): List<Product> {
        return firestore.collection("Product")
            .whereArrayContains("searchKeywords", keyword)
            .get()
            .await()
            .documents
            .mapNotNull { it.toProduct(userId) }
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
    * Firebase Firestore에서 상품 데이터 가져오기
    * */
    private suspend fun DocumentSnapshot.toProduct(userId: String): Product {
        return if (exists() && getBoolean("delete") == false) {
            toObject<Product>()?.copy(
                productId = id,
                images = (get("images") as? List<String>)?.map { imagePath ->
                    getImageUrl(imagePath)
                } ?: emptyList(),
                like = isProductLiked(userId, id)
            ) ?: Product()
        } else {
            Product()
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
}