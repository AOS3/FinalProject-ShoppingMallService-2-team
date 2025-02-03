package com.nemodream.bangkkujaengi.customer.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coupon(
    // 쿠폰 문서 ID
    val documentId: String = "",
    
    // 쿠폰 아이디
    val id : String = "",
    
    // 쿠폰 타이틀
    val title : String = "",
    
    // 쿠폰 조건 설명
    val conditionDescription : String = "",

    // 쿠폰 시작 날짜
    val startCouponDate: Timestamp? = null,

    // 쿠폰 종료 날짜
    val endCouponDate: Timestamp? = null,

    // 쿠폰 사용 가능 기간
    val couponDataLimit : Int = 0,
    
    // 쿠폰이 할인률 인지 할인 가격인지 구분
    val couponType : String = "",
    
    // 쿠폰 할인 가격
    val salePrice : Int = 0,
    
    // 쿠폰 할인율
    val saleRate : Int = 0,
    
    // 얼마 이상 구매해야 하는지 조건
    val morePrice : Int = 0,
    
    // 활성화 or 비활성화
    @PropertyName("activity") val isActivity : Boolean = true,

    // 사용 여뷰 확인
    @PropertyName("use") val isUse: Boolean = false,

    // 보유중
    @PropertyName("hold") val isHold: Boolean = false,
): Parcelable

enum class CouponType(val number: Int, val str: String) {
    SALE_RATE(1, "SALE_RATE"),
    SALE_PRICE(2, "SALE_PRICE");
}