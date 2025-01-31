package com.nemodream.bangkkujaengi.utils

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.R
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/*
* 이미지 로드 확장 함수
* 이미지를 불러오고 있는 중에는 gray_300
* 이미지를 불러오지 못했을 경우에는 gray_500
* url을 받아서 이미지를 불러온다
* */
fun ImageView.loadImage(url: String) {
    Glide.with(context)
        .load(url)
        .centerCrop()
        .placeholder(R.color.gray_300)
        .error(R.color.gray_500)
        .into(this)
}

/*
* SnackBar 확장 함수
* 메세지를 받아 SnackBar로 보여주는 확장함수
* */
fun Context.showSnackBar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        .setAnchorView(
            (this as? AppCompatActivity)?.findViewById<BottomNavigationView>(R.id.customer_bottom_navigation)
        )
        .show()
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/*
* Number의 확장함수로 숫자를 천 단위에서 ,구분하는 함수
* */
fun Number.toCommaString(): String {
    return String.format("%,d", this)
}


fun Timestamp.toFormattedDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(toDate())
}

// 받은 리스트를 드롭다운으로 보여주는 함수
fun Context.createDropDownAdapter(items: List<String>): ArrayAdapter<String> =
    ArrayAdapter(
        this,
        R.layout.item_dropdown_category,
        items
    )

fun Activity.clearFocusOnTouchOutside(event: MotionEvent) {
    if (event.action == MotionEvent.ACTION_DOWN) {
        val view = currentFocus
        if (view is EditText) {
            val outRect = Rect()
            view.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                view.clearFocus()
                view.hideKeyboard()
            }
        }
    }
}

fun Fragment.clearFocusOnTouchOutside(event: MotionEvent) {
    val activity = this.activity ?: return
    activity.clearFocusOnTouchOutside(event)
}

fun Context.getUserType(): String {
    val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("userType", "guest") ?: "guest"
}

fun Context.getUserId(): String {
    val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("documentId", "") ?: ""
}

fun Context.clearUserInfo() {
    // 저장된 유저 정보를 모두 지운다
    val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit { clear() }
}

/* Firestore 게시물 좋아요 토글 확장함수 */
suspend fun FirebaseFirestore.togglePostLikeState(userId: String, postId: String) {
    try {
        this.runTransaction { transaction ->
            // 현재 좋아요 상태만 확인
            val likeDoc = transaction.get(
                this.collection("PostLike").document(userId)
            )

            val currentLikes = if (likeDoc.exists()) {
                (likeDoc.get("postIds") as? List<String>) ?: emptyList()
            } else {
                emptyList()
            }

            // 좋아요 상태 토글
            if (postId in currentLikes) {
                // 좋아요 취소
                transaction.set(
                    this.collection("PostLike").document(userId),
                    mapOf(
                        "userId" to userId,
                        "postIds" to (currentLikes - postId),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                transaction.update(
                    this.collection("Post").document(postId),
                    "likeCount", FieldValue.increment(-1)
                )
            } else {
                // 좋아요 추가
                transaction.set(
                    this.collection("PostLike").document(userId),
                    mapOf(
                        "userId" to userId,
                        "postIds" to (currentLikes + postId),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                transaction.update(
                    this.collection("Post").document(postId),
                    "likeCount", FieldValue.increment(1)
                )
            }
        }.await()
    } catch (e: Exception) {
        Log.e("FirestoreExtension", "게시물 좋아요 상태변경 실패: ", e)
        throw e
    }
}

/* Firestore 게시물 좋아요 여부 확인 확장함수 */
suspend fun FirebaseFirestore.isPostLiked(userId: String, postId: String): Boolean {
    return try {
        val doc = this.collection("PostLike")
            .document(userId)
            .get()
            .await()

        if (doc.exists()) {
            val postIds = doc.get("postIds") as? List<String>
            postIds?.contains(postId) ?: false
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e("FirestoreExtension", "게시물 좋아요 상태확인 실패: ", e)
        false
    }
}

fun Context.showLoginSnackbar(view: View, anchorView: View? = null, action: () -> Unit) {
    Snackbar.make(view, "로그인이 필요한 서비스입니다.", Snackbar.LENGTH_LONG)
        .setAction("로그인") { action() }
        .setActionTextColor(ContextCompat.getColor(this, R.color.white))
        .apply {
            anchorView?.let { this.anchorView = it }
        }
        .show()
}

fun Context.showKeyboard(view: View) {
    getSystemService(InputMethodManager::class.java)?.showSoftInput(
        view,
        InputMethodManager.SHOW_IMPLICIT
    )
}

fun Context.hideKeyboard(view: View) {
    getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(
        view.windowToken,
        0
    )
}