package com.nemodream.bangkkujaengi.customer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val productId: String = "",
    val productName: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val isBest: Boolean = false,
    val category: CategoryType = CategoryType.ALL,
    val price: Int = 0,
    val productCount: Int = 0,
    val saleRate: Int = 0,
    val saledPrice: Int = 0,
    val searchKeywords: List<String> = productName.split(" "),
    val purchaseCount: Int = 0,
    val reviewCount: Int = 0,
    val viewCount: Int = 0,
    val subCategory: SubCategoryType = SubCategoryType.ALL,
    val createdAt: Long = System.currentTimeMillis(),
    val colors: List<String> = emptyList(),
    val delete: Boolean = false,
): Parcelable