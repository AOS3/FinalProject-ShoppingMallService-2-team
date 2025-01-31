package com.nemodream.bangkkujaengi.customer.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class ProfileEditRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {
    suspend fun saveProfileImage(userId: String, updateImageUri: Uri): String {
        return suspendCoroutine { continuation ->
            val storageRef = storage.reference.child("profile/${updateImageUri.lastPathSegment}")

            storageRef.putFile(updateImageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    storageRef.downloadUrl
                }
                .addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    firestore.collection("Member")
                        .document(userId)
                        .update("memberProfileImage", imageUrl)
                        .addOnSuccessListener {
                            continuation.resume(imageUrl)
                        }
                        .addOnFailureListener { e ->
                            continuation.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

}