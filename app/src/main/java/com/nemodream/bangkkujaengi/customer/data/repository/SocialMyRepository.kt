package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
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
class SocialMyRepository @Inject constructor(private val firestore: FirebaseFirestore,) {

    // 현재 로그인된 사용자 정보
    suspend fun getMyProfile(memberDocId: String): Member {
        return firestore.collection("Member")
            .document(memberDocId)
            .get()
            .await()
            .toObject(Member::class.java)!!
    }


    suspend fun getMyWittenPosts(memberDocId: String): List<Post> {
        // 현재 로그인한 사용자의 프로필 가져오기
        val member = getMyProfile(memberDocId)
        val loginUserNickname = member.memberNickName  // 사용자 닉네임

        Log.d("내방꾸", loginUserNickname)

        return firestore.collection("Post")
            .whereEqualTo("nickname", loginUserNickname) // 로그인한 사용자의 닉네임과 일치하는 게시글만 필터링
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toPost()
            }
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


    suspend fun getMySavedPosts(): List<Post> {
        // 테스트용 더미 데이터 생성
        val savedPosts = listOf(
            Post(
                id = "1",
                nickname = "소셜유저",
                authorProfilePicture = "https://example.com/image1.jpg",
                title = "내방소개",
                content = "This is a test post",
                imageList = listOf("https://example.com/image1.jpg"),
                savedCount = 5,
                commentCount = 3,
                productTagPinList = listOf()
            ),
            Post(
                id = "2",
                nickname = "소셜유저",
                authorProfilePicture = "https://example.com/image1.jpg",
                title = "두번째 내방소개",
                content = "Another test post",
                imageList = listOf("https://example.com/image2.jpg"),
                savedCount = 10,
                commentCount = 8,
                productTagPinList = listOf()
            ),
            Post(
                id = "3",
                nickname = "방꾸쟁이유저",
                authorProfilePicture = "https://example.com/image2.jpg",
                title = "첫번째 내방소개",
                content = "Third test post",
                imageList = listOf("https://example.com/image3.jpg"),
                savedCount = 2,
                commentCount = 1,
                productTagPinList = listOf()
            ),
            Post(
                id = "4",
                nickname = "방꾸쟁이유저",
                authorProfilePicture = "https://example.com/image2.jpg",
                title = "다섯번째 내방소개",
                content = "Third test post",
                imageList = listOf("https://example.com/image3.jpg"),
                savedCount = 9,
                commentCount = 0,
                productTagPinList = listOf()
            ),
            Post(
                id = "5",
                nickname = "김혜인",
                authorProfilePicture = "https://example.com/image3.jpg",
                title = "방청소하기",
                content = "Third test post",
                imageList = listOf("https://example.com/image3.jpg"),
                savedCount = 0,
                commentCount = 1,
                productTagPinList = listOf()
            ),
            Post(
                id = "6",
                nickname = "방꾸쟁이",
                authorProfilePicture = "https://example.com/image2.jpg",
                title = "다섯번째 내방소개",
                content = "Third test post",
                imageList = listOf("https://example.com/image3.jpg"),
                savedCount = 9,
                commentCount = 0,
                productTagPinList = listOf()
            ),
            Post(
                id = "7",
                nickname = "방꾸쟁이",
                authorProfilePicture = "https://example.com/image2.jpg",
                title = "다섯번째 내방소개",
                content = "Third test post",
                imageList = listOf("https://example.com/image3.jpg"),
                savedCount = 9,
                commentCount = 0,
                productTagPinList = listOf()
            ),
            Post(
                id = "8",
                nickname = "방꾸쟁이",
                authorProfilePicture = "https://example.com/image2.jpg",
                title = "다섯번째 내방소개",
                content = "Third test post",
                imageList = listOf("https://example.com/image3.jpg"),
                savedCount = 9,
                commentCount = 0,
                productTagPinList = listOf()
            ),
        )
        // 현재 로그인 유저 닉네임으로 필터링
        return savedPosts
    }
}
