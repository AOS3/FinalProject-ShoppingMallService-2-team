package com.nemodream.bangkkujaengi.customer.data.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.customer.data.model.Member
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialFollowingAllRepository @Inject constructor(private val firestore: FirebaseFirestore){

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
}