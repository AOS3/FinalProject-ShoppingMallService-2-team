package com.nemodream.bangkkujaengi.customer.data.model

data class Review(
    val id: String,                     // 리뷰 문서의 ID
    val productId: String,              // 상품 문서의 ID
    val productTitle: String,           // 상품명
    val productImageUrl: String,        // 상품 이미지 URL 추가
    val reviewDate: String = "",        // 리뷰 작성일 (YYYY-MM-DD HH:mm:ss 형식)
    val rating: Int = 0,                // 별점 (1~5)
    val memberId: String,               // 사용자 ID
    val memberNickname: String,
    val memberProfileImage: String,
    val content: String,                // 리뷰 내용
    val isDelete: Boolean = false       // 리뷰 삭제 여부
)
