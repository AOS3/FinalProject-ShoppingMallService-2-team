package com.nemodream.bangkkujaengi.utils

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.nemodream.bangkkujaengi.R
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