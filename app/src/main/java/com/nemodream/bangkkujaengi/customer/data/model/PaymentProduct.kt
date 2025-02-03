package com.nemodream.bangkkujaengi.customer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentProduct (
    var items : List<PaymentItems> = emptyList(),
    var userId : String = ""
) : Parcelable

@Parcelize
data class PaymentItems(
    var productId: String = "",
    var color: String = "",
    var quantity: Int = 0,
    var checked: Boolean = false
) : Parcelable