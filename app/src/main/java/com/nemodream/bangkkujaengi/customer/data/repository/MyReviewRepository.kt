package com.nemodream.bangkkujaengi.customer.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseItem
import com.nemodream.bangkkujaengi.customer.data.model.Review
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MyReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    // Firebase Storage에서 이미지 URL 가져오기
    private suspend fun getImageUrl(imagePath: String): String {
        return storage.reference
            .child(imagePath)
            .downloadUrl
            .await()
            .toString()
    }


    suspend fun fetchProductDataByPurchase(productId: String): ProductData? {
        return try {
            val document = firestore.collection("Product")
                .document(productId)
                .get()
                .await()

            if (!document.exists()) return null

            val productTitle = document.getString("productName") ?: "상품명 없음"
            val imagePath = (document.get("images") as? List<String>)?.getOrNull(0) ?: ""

            // Firebase Storage에서 이미지 URL 가져오기
            val imageUrl = if (imagePath.isNotEmpty()) getImageUrl(imagePath) else ""

            ProductData(productTitle, imageUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 데이터 클래스 정의
    data class ProductData(
        val productTitle: String,
        val imageUrl: String
    )

    suspend fun fetchMemberId(documentId: String): String? {
        return try {
            val document = firestore.collection("Member")
                .document(documentId)
                .get()
                .await()

            document.getString("memberId")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun submitReview(review: Review): Boolean {
        return try {
            // 리뷰 문서 ID가 없으면 Firestore에서 새 문서 ID 생성
            val reviewId = if (review.id.isEmpty()) firestore.collection("Reviews").document().id else review.id

            firestore.collection("Reviews")
                .document(reviewId)
                .set(review.copy(id = reviewId))
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchPurchasesForWrite(memberId: String): List<PurchaseItem> {
        val documents = firestore.collection("Purchase")
            .whereEqualTo("memberId", memberId)
            .whereEqualTo("purchaseState", "PURCHASE_CONFIRMED")
            .get()
            .await()

        // reviewState가 없는 문서만 필터링
        val filteredDocuments = documents.documents.filter { doc ->
            !doc.contains("reviewState")
        }

        return filteredDocuments.map { doc ->
            val productId = doc.getString("productId") ?: ""
            val imagePath = doc.getString("images") ?: ""
            val imageUrl = if (imagePath.isNotEmpty()) getImageUrl(imagePath) else ""
            PurchaseItem(
                productId = productId,
                productTitle = doc.getString("productTitle") ?: "상품명 없음",
                purchaseConfirmedDate = doc.getString("purchaseConfirmedDate") ?: "날짜 없음",
                imageUrl = imageUrl
            )
        }
    }

    suspend fun updateReviewState(productId: String, memberId: String): Boolean {
        return try {
            val purchaseQuery = firestore.collection("Purchase")
                .whereEqualTo("productId", productId)
                .whereEqualTo("memberId", memberId)
                .get()
                .await()

            if (!purchaseQuery.isEmpty) {
                val documentId = purchaseQuery.documents[0].id
                firestore.collection("Purchase")
                    .document(documentId)
                    .update("reviewState", "WRITTEN")  // 상태 변경
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchWrittenReviews(memberId: String): List<Review> {
        val documents = firestore.collection("Reviews")
            .whereEqualTo("memberId", memberId)
            .get()
            .await()

        return documents.map { doc ->
            Review(
                id = doc.id,
                productId = doc.getString("productId") ?: "",
                productTitle = doc.getString("productTitle") ?: "상품명 없음",
                productImageUrl = doc.getString("productImageUrl") ?: "",
                reviewDate = doc.getString("reviewDate") ?: "",
                rating = doc.getLong("rating")?.toInt() ?: 0,
                memberId = doc.getString("memberId") ?: "",
                content = doc.getString("content") ?: "",
                isDelete = doc.getBoolean("isDelete") ?: false,
                memberNickname = doc.getString("memberNickname") ?: "",
                memberProfileImage = doc.getString("memberProfileImage") ?: ""
            )
        }
    }

    suspend fun fetchReviewsForProduct(productId: String): List<Review> {
        return try {
            val documents = firestore.collection("Reviews")
                .whereEqualTo("productId", productId)
                .get()
                .await()

            val memberIds = documents.mapNotNull { it.getString("memberId") }.distinct()

            // 모든 memberId에 대한 닉네임과 프로필 사진 가져오기
            val memberInfoMap = fetchNicknamesWithProfileImage(memberIds)

            // 리뷰에 닉네임과 프로필 사진 매핑
            documents.map { doc ->
                val memberId = doc.getString("memberId") ?: ""
                val (nickname, profileImage) = memberInfoMap[memberId] ?: ("닉네임 없음" to "")

                Review(
                    id = doc.id,
                    productId = doc.getString("productId") ?: "",
                    productTitle = doc.getString("productTitle") ?: "",
                    productImageUrl = doc.getString("productImageUrl") ?: "",
                    memberId = memberId,
                    memberNickname = nickname,
                    memberProfileImage = profileImage,
                    reviewDate = doc.getString("reviewDate") ?: "",
                    rating = doc.getLong("rating")?.toInt() ?: 0,
                    content = doc.getString("content") ?: "",
                    isDelete = doc.getBoolean("isDelete") ?: false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun fetchNicknamesWithProfileImage(memberIds: List<String>): Map<String, Pair<String, String>> {
        return try {
            val documents = firestore.collection("Member")
                .whereIn("memberId", memberIds)
                .get()
                .await()

            documents.associate { doc ->
                val memberId = doc.getString("memberId")!!
                val nickname = doc.getString("memberNickName") ?: "닉네임 없음"
                val profileImage = doc.getString("memberProfileImage") ?: "https://www.studiopeople.kr/common/img/default_profile.png"
                memberId to (nickname to profileImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }


}
