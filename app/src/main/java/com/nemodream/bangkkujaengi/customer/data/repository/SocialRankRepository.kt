package com.nemodream.bangkkujaengi.customer.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.data.model.Tag
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRankRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {

    suspend fun getPostsBySavedDecending(): List<Post> =
        firestore.collection("Post")
            // 저장 수 내림차순 정렬
            .orderBy("savedCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toPost()
            }

    // Firebase Firestore에서 게시글 데이터 가져오기
    private fun DocumentSnapshot.toPost(): Post? {
        val id = getString("id") ?: return null
        val memberDocId = getString("memberDocId") ?: return null
        val nickname = getString("nickname") ?: return null
        val authorProfilePicture = getString("authorProfilePicture") ?: return null
        val title = getString("title") ?: return null
        val content = getString("content") ?: return null
        val imageList = get("imageList") as? List<String> ?: emptyList()
        val savedCount = getLong("savedCount")?.toInt() ?: 0
        val commentCount = getLong("commentCount")?.toInt() ?: 0
        val productTagPinList = get("productTagPinList") as? List<Tag> ?: emptyList()


        return Post(
            id = id,
            memberDocId = memberDocId,
            nickname = nickname,
            authorProfilePicture = authorProfilePicture,
            title = title,
            content = content,
            imageList = imageList,
            savedCount = savedCount,
            commentCount = commentCount,
            productTagPinList = productTagPinList
        )
    }
}
