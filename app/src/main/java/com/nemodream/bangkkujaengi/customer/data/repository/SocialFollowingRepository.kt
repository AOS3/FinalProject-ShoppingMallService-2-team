package com.nemodream.bangkkujaengi.customer.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.data.model.Tag
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialFollowingRepository @Inject constructor(private val firestore: FirebaseFirestore){

    // 현재 로그인된 사용자 정보
    suspend fun getMyProfile(memberDocId: String): Member {
        return firestore.collection("Member")
            .document(memberDocId)
            .get()
            .await()
            .toObject(Member::class.java)!!
    }

    /**
     * 사용자가 팔로잉 중인 회원 목록을 가져온다
     */
    suspend fun getFollowingMembers(memberDocId: String): List<Member> {
        val memberDoc = firestore.collection("Member")
            .document(memberDocId)
            .get()
            .await()

        val followingList = memberDoc.get("followingList") as? List<DocumentReference> ?: emptyList()

        return followingList.mapNotNull { ref ->
            ref.get().await().toObject(Member::class.java)
        }
    }

    suspend fun getPostsByMember(member: Member): List<Post> {
        return firestore.collection("Post")
            .whereEqualTo("nickname", member.memberNickName) // 선택한 사용자의 닉네임과 일치하는 게시글만 필터링
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
}