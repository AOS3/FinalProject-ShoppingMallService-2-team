package com.nemodream.bangkkujaengi.customer.deliverytracking

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackingResponse(
    val invoiceNo: String = "",      // 운송장 번호
    val itemName: String = "",       // 상품 이름
    val receiverName: String = "",   // 수령인 이름
    val receiverAddr: String = "",   // 수령인 주소
    val trackingDetails: List<TrackingDetail> = emptyList(), // 상세 내역
    val level: Int = 0,             // 배송 단계
    val complete: Boolean = false       // 배송 완료 여부
) : Parcelable

@Parcelize
data class TrackingDetail(
    val timeString: String = "", // 진행 시간
    val where: String = "",      // 위치
    val kind: String = ""        // 진행 상태
) : Parcelable
