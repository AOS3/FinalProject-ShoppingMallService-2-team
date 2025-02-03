package com.nemodream.bangkkujaengi.customer.data.model

data class Tag(
    val order: Int? = null, // 사진이 몇번째인지의 정보
    val tagX: Float ? = null, // 태그핀의 x위치
    val tagY: Float ? = null, // 태그핀의 y위치
    val tagProductInfo: Product ? = null // 태그핀이 가지고 있는 태그 상품 정보
)