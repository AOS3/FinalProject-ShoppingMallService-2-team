package com.nemodream.bangkkujaengi.customer.data.model

import com.google.firebase.firestore.DocumentReference

data class Member(

    // 문서 id
    val id: String = "",
    // 이름
    val memberName: String = "",
    // 아이디
    val memberId: String = "",
    // 닉네임
    val memberNickName : String = "",
    // 비밀번호
    val memberPassword: String = "",
    // 전화번호
    val memberPhoneNumber: String = "",
    // 프로필 사진
    val memberProfileImage: String = "https://www.studiopeople.kr/common/img/default_profile.png",
    // 적립금
    val point: Int = 3000,
    // 탈퇴 여부
    val isActive: Boolean = true,
    // 회원가입 시간
    val createAt: Long = System.currentTimeMillis(),
    // 소셜 로그인 여부
    val socialLogin: SocialLogin = SocialLogin.NONE,
    // 찜한 상품
    val isLikeList: List<String> = emptyList(),
    // 팔로잉 수
    val followingCount:Int = 0,
    // 팔로워 수
    val followerCount:Int = 0,
    // 팔로잉 목록
    val followingList: List<DocumentReference> = emptyList(),
    // 올린 게시글 목록
    val postedList: List<Post> = emptyList(),
    // 저장한 게시글 리스트
    val savedPost: List<Post> = emptyList(),
    // 쿠폰 문서 id 리스트
    val couponDocumentId: List<String> = emptyList(),
)

enum class SocialLogin(val number:Int, val str:String){
    NONE(0, "일반 로그인"),
    KAKAO(1, "카카오 로그인"),
    GOOGLE(2, "구글 로그인")
}