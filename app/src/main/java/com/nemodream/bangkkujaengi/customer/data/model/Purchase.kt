package com.nemodream.bangkkujaengi.customer.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Purchase(
    val documentId: String = "",
    val purchaseId: String = "",
    val nonMemberPassword: String = "",
    val memberId: String = "",
    val productTitle : String = "",
    val color : String = "",
    val images : String = "",
    val productId : String = "",
    val productCost : Int = 0,
    val couponSalePrice : Int = 0,
    val saleRate : Int = 0,
    val totPrice : Int = 0,
    val purchaseDate : Timestamp = Timestamp.now(),
    val purchaseState: String = "",
    val purchaseInvoiceNumber : Long  = 0,
    val purchaseQuantity : Int = 0,
    val Delete: Boolean = false,
    val purchaseDateTime: String = "",
    val deliveryCost: Int = 0,
    val receiverName: String = "",
    val receiverZipCode: String = "",
    val receiverAddr: String = "",
    val receiverDetailAddr: String = "",
    val receiverPhone: String = "",
    val purchaseConfirmedTime: Timestamp? = null
) : Parcelable

@Parcelize
enum class PurchaseState() : Parcelable {
    PAYMENT_COMPLETED,   // 결제 완료
    PRODUCT_READY,       // 상품 준비
    SHIPPING,            // 배송 중
    PURCHASE_CONFIRMED,  // 구매 확정
    CANCELED             // 취소
}