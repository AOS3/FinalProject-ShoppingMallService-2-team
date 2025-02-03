package com.nemodream.bangkkujaengi.customer.deliverytracking

import retrofit2.http.GET
import retrofit2.http.Query

interface TrackingApi {
    @GET("/api/v1/trackingInfo")
    suspend fun getTrackingInfo(
        @Query("t_key") apiKey: String,       // API Key
        @Query("t_code") courierCode: String, // 택배사 코드
        @Query("t_invoice") invoiceNumber: String // 운송장 번호
    ): TrackingResponse
}
