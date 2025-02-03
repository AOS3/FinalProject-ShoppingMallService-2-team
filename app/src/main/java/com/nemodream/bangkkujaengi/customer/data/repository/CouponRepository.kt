package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    // Firebase에서 Coupon 정보를 activity가 true인 쿠폰만 가져오는 함수
    suspend fun getCouponList(memberId: String) = try {
        // 멤버의 쿠폰 목록 가져오기
        val memberSnapshot = firestore.collection("Member")
            .document(memberId)
            .get()
            .await()

        val memberCoupons = memberSnapshot.get("couponDocumentId") as? List<String> ?: emptyList()

        // 활성화된 전체 쿠폰 가져와서 보유 여부 체크
        val snapshot = firestore.collection("Coupon")
            .whereEqualTo("activity", true)
            .whereEqualTo("use", false)
            .get()
            .await()

        snapshot.toObjects(Coupon::class.java).map { coupon ->
            coupon.copy(isHold = memberCoupons.contains(coupon.documentId))
        }
    } catch (e: Exception) {
        Log.e("getCouponList", "Error getting coupon list: ${e.message}", e)
        emptyList()
    }

    // 현재 멤버에게 쿠폰 id를 저장한다.
    fun receiveCoupon(userId: String, coupon: Coupon) {
        firestore.collection("Member")
            .document(userId)
            .update("couponDocumentId", FieldValue.arrayUnion(coupon.documentId))
    }
}