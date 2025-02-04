package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.Cart
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.ShoppingCart
import kotlinx.coroutines.tasks.await

class ShoppingCartRepository {

    companion object {

        // 문서 id로 userId 가져오기
        suspend fun getting_user_id_by_document_id(document_id: String): String {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Member")

            // Firestore에서 해당 document_id가 있는 문서 조회
            val documentSnapshot = collectionReference.document(document_id).get().await()

            val memberId = documentSnapshot.getString("memberId")
            Log.d("FirestoreCheck", "User ID: $memberId")
            return memberId!! // 반환
        }

        // 유저 id로 장바구니 아이템 정보 가져오기
        suspend fun getting_shopping_cart_item_by_userId(user_id: String): MutableList<Map<String, *>> {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Cart")
            val result = collectionReference.whereEqualTo("userId", user_id).get().await()

            val shopping_cart_item_list = mutableListOf<Map<String, *>>()

            result.forEach {document ->
                val cartData = document.toObject(ShoppingCart::class.java) // ShoppingCart로 매핑

//                // Cart 데이터를 로그로 출력
//                Log.d("test200", "Cart Data: ${cartData.items}")

                val map = mapOf(
                    "cart_document_id" to document.id,
                    "cart_data" to cartData
                )
                shopping_cart_item_list.add(map)
            }

            Log.d("test1010", "${shopping_cart_item_list}")

            shopping_cart_item_list.map {
                val cartData = it["cart_data"] as? ShoppingCart // cart_data를 ShoppingCart로 안전하게 캐스팅
                cartData?.items?.forEach { cart ->
                    Log.d("test200", "Product ID: ${cart.productId}") // 각 Cart 객체의 productId 출력
                }
            }

            // Log.d("test200", "gettingShoppingCartItemByUserId: ${shopping_cart_item_list[0]["cart_data"]}")

            return shopping_cart_item_list
        }

        // 상품 document id로 상품 정보 가져오기
        suspend fun getting_prodcut_by_product_document_id(product_document_id_list: List<String>): MutableList<Map<String, *>> {
            Log.d("test200", "test2: ${product_document_id_list}")
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Product")

            // 결과를 저장할 리스트
            val product_list = mutableListOf<Map<String, *>>()

            product_document_id_list.forEach {
                val result = collectionReference.document(it).get().await()
                val map = mapOf(
                    "product_document_id" to it,
                    "product_data" to result.toObject(Product::class.java)
                )
                product_list.add(map)
            }

            product_list.forEach {
                Log.d("cccc", "getting_prodcut_by_product_document_id: ${it["product_document_id"]}")
                Log.d("cccc", "getting_prodcut_by_product_document_id: ${it["product_data"]}")
            }

            return product_list
        }

        // test
//        suspend fun add_cart_items_by_product_ids(user_id: String) {
//            Log.d("add_cart_item", "여기옴")
//            val firestore = FirebaseFirestore.getInstance()
//            val collectionReference = firestore.collection("Cart")
//            val result = collectionReference.whereEqualTo("userId", user_id).get().await()
//
//            val product_id_list = listOf("AxXC1DVb8zsE92nyit8N", "L0JpdVtcyvEkbHMM0Oms", "NI7Dn3p4s1wgzwO7DKWq", "e4L8vI6zDw9iQdObqqIo", "qHwOL6xAsO5JOTdLlBd2")
//
//            // 장바구니가 이미 존재하는지 확인
//            if (result.isEmpty) {
//                // 장바구니가 없으면 새로운 장바구니를 생성하고 모든 상품을 추가
//                val newItems = product_id_list.map { productId ->
//                    Cart(productId = productId, quantity = 1, checked = false)
//                }
//                val newCart = ShoppingCart(
//                    userId = user_id,
//                    items = newItems
//                )
//                collectionReference.add(newCart).await()
//                Log.d("add_cart_item", "New cart created and products added for user: $user_id")
//            } else {
//                // 기존 장바구니에 상품 추가
//                val documentReference = result.documents.first().reference
//                val shoppingCart = result.documents.first().toObject(ShoppingCart::class.java)
//
//                // 기존 items 리스트에 새 상품 추가
//                val updatedItems = shoppingCart?.items?.toMutableList() ?: mutableListOf()
//
//                product_id_list.forEach { productIdToAdd ->
//                    val existingItem = updatedItems.find { it.productId == productIdToAdd }
//                    if (existingItem != null) {
//                        // 이미 존재하는 상품인 경우 수량 증가
//                        existingItem.quantity += 1
//                    } else {
//                        // 새로운 상품인 경우 추가
//                        updatedItems.add(Cart(productId = productIdToAdd, quantity = 1, checked = false))
//                    }
//                }
//
//                // Firestore에 업데이트
//                val updateMap = mapOf("items" to updatedItems.map { it.copy() }) // 복사본으로 Firestore 업데이트
//                documentReference.update(updateMap).await()
//                Log.d("add_cart_item", "Products added/updated in cart for user: $user_id")
//            }
//        }

        suspend fun add_cart_item_by_product_id(
            user_id: String,
            productId: String,
            quantity: Int,
            color: String
        ) {
            Log.d("add_cart_item", "여기옴")
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Cart")

            // userId 필드가 user_id와 일치하는 문서를 조회
            val querySnapshot = collectionReference
                .whereEqualTo("userId", user_id)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                // 해당 사용자의 Cart 문서가 없으면 새로 생성 (문서 id는 자동 생성됨)
                val newItem = Cart(
                    productId = productId,
                    quantity = quantity,
                    color = color,
                    checked = false
                )
                val newCart = ShoppingCart(
                    userId = user_id,
                    items = listOf(newItem)
                )
                collectionReference.add(newCart).await()
                Log.d("add_cart_item", "New cart created for user: $user_id")
            } else {
                // 이미 해당 사용자의 Cart 문서가 있으면 첫 번째 문서를 업데이트
                val documentReference = querySnapshot.documents.first().reference
                val shoppingCart = querySnapshot.documents.first().toObject(ShoppingCart::class.java)
                val updatedItems = shoppingCart?.items?.toMutableList() ?: mutableListOf()

                // 동일한 productId가 있는지 확인 후, 있으면 수량 증가, 없으면 새 항목 추가
                val existingItem = updatedItems.find { it.productId == productId }
                if (existingItem != null) {
                    existingItem.quantity += quantity
                    if (color.isNotEmpty()) {
                        existingItem.color = color
                    }
                } else {
                    updatedItems.add(
                        Cart(
                            productId = productId,
                            quantity = quantity,
                            color = color,
                            checked = false
                        )
                    )
                }
                documentReference.update("items", updatedItems).await()
                Log.d("add_cart_item", "Cart updated for user: $user_id")
            }
        }




        // 장바구니 아이템 갯수 업데이트
        suspend fun update_cart_item_quantity(userId: String, productId: String, quantity: Int) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Cart")

            try {
                // userId로 문서 검색
                val querySnapshot = collectionReference.whereEqualTo("userId", userId).get().await()

                if (!querySnapshot.isEmpty) {
                    // 첫 번째 문서 가져오기 (userId로 검색했으므로 한 문서만 있다고 가정)
                    val document = querySnapshot.documents[0]
                    val shoppingCart = document.toObject(ShoppingCart::class.java)

                    // items 리스트를 업데이트
                    val updatedItems = shoppingCart?.items?.map { cart ->
                        if (cart.productId == productId) {
                            cart.copy(quantity = quantity) // quantity 값을 업데이트
                        } else {
                            cart
                        }
                    }

                    // 수정할 데이터를 맵에 담기
                    val updateMap = mapOf(
                        "items" to updatedItems
                    )

                    // Firestore에 수정된 데이터 업데이트
                    collectionReference.document(document.id).update(updateMap).await()

                    Log.d("CartUpdate", "Updated quantity for productId: $productId in userId: $userId")
                } else {
                    Log.e("FirestoreError", "No cart found for userId: $userId")
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error updating cart item quantity: ${e.message}", e)
            }
        }


        // 장바구니 아이템 체크 업데이트
        suspend fun update_cart_item_checked_by_user_id(userId: String, productId: String, checked: Boolean) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Cart")

            try {
                // userId로 문서 검색
                val querySnapshot = collectionReference.whereEqualTo("userId", userId).get().await()

                if (!querySnapshot.isEmpty) {
                    // 첫 번째 문서 가져오기 (userId로 검색했으므로 한 문서만 있다고 가정)
                    val document = querySnapshot.documents[0]
                    val shoppingCart = document.toObject(ShoppingCart::class.java)

                    // items 리스트를 업데이트
                    val updatedItems = shoppingCart?.items?.map { cart ->
                        if (cart.productId == productId) {
                            cart.copy(checked = checked) // checked 값을 업데이트
                        } else {
                            cart
                        }
                    }

                    // Firestore에 수정된 데이터 업데이트
                    val updateMap = mapOf(
                        "items" to updatedItems
                    )
                    collectionReference.document(document.id).update(updateMap).await()

                    Log.d("Firestore", "Cart item updated for userId: $userId, productId: $productId")
                } else {
                    Log.e("FirestoreError", "No document found for userId: $userId")
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error updating cart item: ${e.message}", e)
            }
        }




        // 장바구니 아이템을 삭제하는 메소드 (userId 기준으로 조회)
        suspend fun delete_cart_item_by_product_id(user_id: String, product_id: String) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Cart")

            // 유저의 장바구니 데이터 가져오기
            val result = collectionReference.whereEqualTo("userId", user_id).get().await()

            if (result.isEmpty) {
                Log.d("delete_cart_item", "No cart found for user: $user_id")
                return
            }

            // 첫 번째 장바구니 문서 가져오기 (단일 장바구니 가정)
            val documentReference = result.documents.first().reference
            val shoppingCart = result.documents.first().toObject(ShoppingCart::class.java)

            if (shoppingCart != null) {
                // 삭제할 상품을 제외한 새로운 items 리스트 생성
                val updatedItems = shoppingCart.items.filter { it.productId != product_id }

                // Firestore의 items 배열 업데이트
                val updateMap = mapOf("items" to updatedItems)
                documentReference.update(updateMap).await()

                Log.d("delete_cart_item", "Product $product_id deleted for user: $user_id")
            } else {
                Log.d("delete_cart_item", "Cart data is null for user: $user_id")
            }
        }


        // 체크된 상품 목록 삭제
        suspend fun delete_cart_item_by_checked(user_id: String) {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Cart")

            // 유저의 장바구니 데이터 가져오기
            val result = collectionReference.whereEqualTo("userId", user_id).get().await()

            if (result.isEmpty) {
                Log.d("delete_cart_item", "No cart found for user: $user_id")
                return
            }

            // 첫 번째 장바구니 문서 가져오기 (단일 장바구니 가정)
            val documentReference = result.documents.first().reference
            val shoppingCart = result.documents.first().toObject(ShoppingCart::class.java)

            if (shoppingCart != null) {
                // checked가 true인 항목 제외한 새로운 items 리스트 생성
                val updatedItems = shoppingCart.items.filter { !it.checked }

                // Firestore의 items 배열 업데이트
                val updateMap = mapOf("items" to updatedItems)
                documentReference.update(updateMap).await()

                Log.d("delete_cart_item", "Checked items deleted for user: $user_id")
            } else {
                Log.d("delete_cart_item", "Cart data is null for user: $user_id")
            }
        }


        // 이미지 url을 가져오는 메소드
        suspend fun getting_image(image_url: String) : String {
            val storageReference = FirebaseStorage.getInstance().reference
            // 파일명을 지정하여 이미지 데이터를 가져온다.
            val childStorageReference = storageReference.child("$image_url")
            val imageUri = childStorageReference.downloadUrl.await().toString()

            return imageUri
        }

        // 유저 id를 통해 유저 데이터 가져오는 메소드
        suspend fun getting_user_data_by_user_id(user_id: String): Map<String, *>? {
            val firestore = FirebaseFirestore.getInstance()
            val collectionReference = firestore.collection("Member")

            // Firestore에서 memberId 필드로 사용자 검색
            val result = collectionReference.whereEqualTo("memberId", user_id).get().await()

            if (result.documents.isNotEmpty()) {
                val document = result.documents[0] // 첫 번째 문서 가져오기



                val member = Member(
                    id = document.id,
                    memberId = document.getString("memberId") ?: "",
                    memberPassword = document.getString("memberPassword") ?: "",
                    memberName = document.getString("memberName") ?: "",
                    memberNickName = document.getString("memberNickName") ?: "",
                    memberPhoneNumber = document.getString("memberPhoneNumber") ?: "", // 처리된 전화번호
                    memberProfileImage = document.getString("memberProfileImage") ?: "https://www.studiopeople.kr/common/img/default_profile.png",
                    point = document.getLong("point")?.toInt() ?: 3000,
                    isActive = document.getBoolean("isActive") ?: false,
                    createAt = document.getLong("createAt") ?: 0L,
                    followingCount = document.getLong("followingCount")?.toInt() ?: 0,
                    followingList = emptyList(), // 리스트 매핑 필요 시 처리
                    couponDocumentId = document.get("couponDocumentId") as? List<String>
                        ?: emptyList(),
                    followerCount = document.getLong("followerCount")?.toInt() ?: 0,
                )

                return mapOf(
                    "member_document_id" to document.id,
                    "member_data" to member
                )
            }

            return null // 문서가 없을 경우 null 반환
        }



    }

}