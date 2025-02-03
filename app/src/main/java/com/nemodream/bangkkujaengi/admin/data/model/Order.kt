package com.nemodream.bangkkujaengi.admin.data.model

data class Order(
    val orderDate: String?,               // 주문일시
    val productName: String,              // 상품명
    val customerId: String,               // 구매자 ID
    val orderNumber: String,              // 주문번호
    val deliveryStatus: String? = null,   // 배송 상태 (배송 탭에 표시)
    val invoiceNumber: String? = null,    // 송장번호 (배송 탭에 표시)
    val deliveryStartDate: String? = null,// 배송 시작일
    val deliveryDate: String? = "--",     // 배송 완료일 (배송 탭에 표시)
    val confirmationDate: String? = null, // 구매 확정일 (구매 확정 탭에 표시)
    val cancelDate: String? = null,       // 취소일시 (취소 탭에 표시)
    val canceledBy: String? = null,       // 취소 주체 (구매자/판매자 - 취소 탭에 표시)
    val cancellationReason: String? = null, // 취소 사유 (취소 탭에 표시)
    val state: OrderState                 // 주문 상태
)

enum class OrderState {
    PAYMENT_COMPLETED,   // 결제 완료
    PRODUCT_READY,       // 상품 준비
    SHIPPING,            // 배송 중
    PURCHASE_CONFIRMED,  // 구매 확정
    CANCELED             // 취소
}
