package com.nemodream.bangkkujaengi.admin.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminCouponRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    // 쿠폰 데이터 Coupon 컬렉션에 저장하기
    suspend fun saveCoupon(coupon: Coupon) {
        firestore.collection("Coupon").add(coupon).await()
    }

    // 쿠폰 정보 읽어오기
    suspend fun loadCoupon(): List<Coupon> = try {
        val snapshot = firestore.collection("Coupon").get().await()
        snapshot.documents.map { document ->
            document.toObject(Coupon::class.java)?.copy(
                documentId = document.id
            ) ?: throw Exception("쿠폰 정보 불러오기 실패")
        }
    } catch (e: Exception) {
        throw e
    }

    // 쿠폰 삭제하기
    fun deleteCoupon(coupon: Coupon) {
        firestore.collection("Coupon").document(coupon.documentId).delete()
    }

}