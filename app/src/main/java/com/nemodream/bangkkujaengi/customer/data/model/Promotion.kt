package com.nemodream.bangkkujaengi.customer.data.model

data class Promotion(
    val title: String = "",                          // 프로모션 제목
    val order: Int = 0,                             // 프로모션 정렬 순서
    val isActive: Boolean = true,                   // 프로모션 활성화 여부
    val productIds: List<String> = emptyList(),     // 프로모션 상품 ID 목록
    val filterField: String? = null,                // 필터링할 필드 (예: "purchaseCount", "price" 등)
    val filterValue: Int? = null,                   // 필터링 기준값
    val filterType: String? = null,                 // 필터링 타입 (예: "GREATER_THAN", "LESS_THAN" 등)
)

sealed class PromotionItem
data class PromotionHeader(val title: String) : PromotionItem()
data class PromotionProducts(val products: List<Product>) : PromotionItem()