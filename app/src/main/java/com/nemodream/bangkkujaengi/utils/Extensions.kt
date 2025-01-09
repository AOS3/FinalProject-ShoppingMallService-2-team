package com.nemodream.bangkkujaengi.utils

import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.nemodream.bangkkujaengi.R

/*
* BottomNavigation에서 화면을 전환할때 활용하는 함수
* */
fun Fragment.navigateToChildFragment(fragment: Fragment) {
    childFragmentManager.commit {
        replace(R.id.customer_container, fragment)
    }
}

/*
* ParentFragment 화면을 변경하는 함수
* 새로운 화면을 BottomNavigation을 공유하지 않고, 가리는 목적으로 활용
* */
fun Fragment.replaceParentFragment(fragment: Fragment, tag: String) {
    requireParentFragment().parentFragment?.parentFragmentManager?.commit {
        replace(R.id.parent_container, fragment)
        addToBackStack(tag)
    }
}

/*
* 이미지 로드 확장 함수
* 이미지를 불러오고 있는 중에는 gray_300
* 이미지를 불러오지 못했을 경우에는 gray_500
* url을 받아서 이미지를 불러온다
* */
fun ImageView.loadImage(url: String) {
    Glide.with(context)
        .load(url)
        .placeholder(R.color.gray_300)
        .error(R.color.gray_500)
        .into(this)
}

/*
* SnackBar 확장 함수
* 메세지를 받아 SnackBar로 보여주는 확장함수
* */
fun Fragment.showSnackBar(message: String) {
    view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
}
