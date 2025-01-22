package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.CategoryType
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SortType
import com.nemodream.bangkkujaengi.customer.data.model.SubCategoryType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {

    /*
    * 카테고리별 상품 리스트 가져오기
    * 대분류 카테고리와 소분류 카테고리로 데이터 가져온다.
    * */
    suspend fun getProducts(
        category: CategoryType,
        subCategory: SubCategoryType,
        sortType: SortType
    ): List<Product> {
        return try {
            Log.d("ProductRepository", "Fetching products with category: $category, subCategory: ${subCategory.title}")

            // 기본 쿼리 생성
            var query = when (category) {
                CategoryType.ALL -> {
                    Log.d("ProductRepository", "Getting ALL products without filtering")
                    firestore.collection(COLLECTION_PRODUCTS)
                }
                else -> {
                    Log.d("ProductRepository", "Filtering by category: ${category.name}")
                    firestore.collection(COLLECTION_PRODUCTS)
                        .whereEqualTo("category", category.name)
                        .let { baseQuery ->
                            if (subCategory.title != "전체") {
                                baseQuery.whereEqualTo("subCategory", subCategory.name)
                            } else {
                                baseQuery
                            }
                        }
                }
            }

            // 정렬 조건 추가
            query = when (sortType) {
                SortType.PURCHASE -> query.orderBy("purchaseCount", Query.Direction.DESCENDING)
                SortType.REVIEW -> query.orderBy("reviewCount", Query.Direction.DESCENDING)
                SortType.PRICE_HIGH -> query.orderBy("price", Query.Direction.DESCENDING)
                SortType.PRICE_LOW -> query.orderBy("price", Query.Direction.ASCENDING)
                SortType.VIEWS -> query.orderBy("viewCount", Query.Direction.DESCENDING)
                SortType.LATEST -> query.orderBy("createdAt", Query.Direction.DESCENDING)
                SortType.DISCOUNT -> query.orderBy("saleRate", Query.Direction.DESCENDING)
            }

            val documents = query.get().await().documents
            Log.d("ProductRepository", "Query returned ${documents.size} documents")

            documents.mapNotNull { document ->
                try {
                    document.toProduct()
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Query failed", e)
            emptyList()
        }
    }

    private suspend fun DocumentSnapshot.toProduct(): Product {
        val imageUrls = (get("images") as? List<String>)?.map { imagePath ->
            getImageUrl(imagePath)
        } ?: emptyList()

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
            saleRate = getLong("saleRate")?.toInt() ?: 0
        )
    }

    private suspend fun getImageUrl(imagePath: String) = storage.reference
        .child(imagePath)
        .downloadUrl
        .await()
        .toString()

    companion object {
        private const val COLLECTION_PRODUCTS = "Product"
    }
}