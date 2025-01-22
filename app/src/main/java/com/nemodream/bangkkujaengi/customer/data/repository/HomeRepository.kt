package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.Banner
import com.nemodream.bangkkujaengi.customer.data.model.Cart
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.Promotion
import com.nemodream.bangkkujaengi.customer.data.model.PromotionHeader
import com.nemodream.bangkkujaengi.customer.data.model.PromotionItem
import com.nemodream.bangkkujaengi.customer.data.model.PromotionProducts
import com.nemodream.bangkkujaengi.customer.data.model.SubCategoryType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {
    /*
    * 배너 데이터 가져오기
    * */
    suspend fun getBanners(): List<Banner> =
        firestore.collection("Banner")
            .get()
            .await()
            .documents
            .map { doc ->
                doc.toBanner().copy(thumbnailImageRef = getImageUrl(doc.getString("imageUrl") ?: ""))
            }
    /*
    * Firebase Storage에서 상대 경로에 맞는 이미지 가져오기
    * */
    private suspend fun getImageUrl(imagePath: String) = storage.reference
        .child(imagePath)
        .downloadUrl
        .await()
        .toString()

    /*
    * productId를 통해 Firebase의 Product Collection에서 해당 상품 데이터 가져오기
    * */
    suspend fun getProducts(productId: String): Product =
        firestore.collection("Product")
            .document(productId)
            .get()
            .await()
            .toProduct()

    /*
    * Firebase Firestore에서 배너 데이터 가져오기
    * */
    private fun DocumentSnapshot.toBanner(): Banner =
        Banner(
            id = id,
            productId = getString("productId") ?: "",
            thumbnailImageRef = getString("imageUrl") ?: ""
        )

    /*
    * Firebase Firestore에서 상품 데이터 가져오기
    * */
    private suspend fun DocumentSnapshot.toProduct(): Product {
        val imageRefs = get("images") as? List<String> ?: emptyList()
        val imageUrls = imageRefs.map { imagePath -> getImageUrl(imagePath) }

        return Product(
            productId = id,
            productName = getString("productName") ?: "",
            description = getString("description") ?: "",
            images = imageUrls,
            isBest = getBoolean("best") ?: false,
            category = CategoryType.fromString(getString("category") ?: ""),
            subCategory = SubCategoryType.fromString(getString("subCategory") ?: ""),
            price = getLong("price")?.toInt() ?: 0,
            productCount = getLong("productCount")?.toInt() ?: 0,
            saledPrice = getLong("saledPrice")?.toInt() ?: 0,
            saleRate = getLong("saledRate")?.toInt() ?: 0,
            colors = get("colors") as? List<String> ?: emptyList()
        )
    }

    /*
    * Firebase Firestore에서 프로모션 데이터 가져오기
    * */
    private fun DocumentSnapshot.toPromotion(): Promotion =
        Promotion(
            title = getString("title") ?: "",
            order = getLong("order")?.toInt() ?: 0,
            isActive = getBoolean("isActive") ?: false,
            startDate = getTimestamp("startDate")?.seconds ?: 0L,
            endDate = getTimestamp("endDate")?.seconds ?: 0L,
            productIds = (get("productIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )


    fun saveCartProduct(productId: String, quantity: Int = 1, color: String) {
        val userCartRef = firestore.collection("Cart").document("testuser")

        userCartRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Map으로 직접 데이터 처리
                    val items = (document.get("items") as? List<Map<String, Any>>)?.map {
                        Cart(
                            productId = it["productId"] as String,
                            quantity = (it["quantity"] as Long).toInt(),
                            color = it["color"] as String,
                        )
                    } ?: listOf()

                    val existingItem = items.find { it.productId == productId }

                    val updatedItems = if (existingItem != null) {
                        val newQuantity = existingItem.quantity + quantity
                        if (newQuantity > 10) {
                            return@addOnSuccessListener
                        }

                        items.map {
                            if (it.productId == productId) Cart(productId, newQuantity, color)
                            else it
                        }
                    } else {
                        items + Cart(productId, quantity, color)
                    }

                    // Map으로 변환하여 저장
                    val itemsToSave = updatedItems.map {
                        mapOf(
                            "productId" to it.productId,
                            "quantity" to it.quantity,
                            "color" to it.color,
                        )
                    }

                    userCartRef.update("items", itemsToSave)
                } else {
                    // 새 장바구니 생성
                    val cartData = mapOf(
                        "userId" to "testuser",
                        "items" to listOf(
                            mapOf(
                                "productId" to productId,
                                "quantity" to quantity,
                                "color" to color,
                            )
                        ),
                        "isDelete" to false
                    )
                    userCartRef.set(cartData)
                }
            }
    }

    suspend fun getPromotionSections(): List<PromotionItem> {
        val sections = mutableListOf<PromotionItem>()

        try {
            val promotions = firestore.collection("Promotion")
                .orderBy("order")
                .get()
                .await()

            promotions.documents.forEach { doc ->
                val promotion = doc.toPromotion()

                if (promotion.isActive) {
                    sections.add(PromotionHeader(promotion.title))
                    val products = getProductsByIds(promotion.productIds)
                    sections.add(PromotionProducts(products))
                }
            }
        } catch (e: Exception) {
            Log.e("PromotionRepository", "getPromotionSections error: ", e)
        }

        return sections
    }

    private suspend fun getProductsByIds(productIds: List<String>): List<Product> {
        return productIds.mapNotNull { productId ->
            try {
                getProducts(productId)
            } catch (e: Exception) {
                Log.e("PromotionRepository", "Error fetching product $productId: ", e)
                null
            }
        }
    }

}