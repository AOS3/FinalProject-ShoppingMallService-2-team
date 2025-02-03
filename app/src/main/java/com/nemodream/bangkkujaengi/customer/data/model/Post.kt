package com.nemodream.bangkkujaengi.customer.data.model

data class Post(
    // 게시글 고유 아이디
    val id: String = "",
    // 작성자 고유 아이디
    val memberDocId: String = "",
    // 작성자 닉네임
    val nickname: String = "",
    // 작성자 프사
    val authorProfilePicture: String = "",
    // 게시글 제목
    val title: String = "",
    // 게시글 내용
    val content: String = "",
    // 게시 사진들
    val imageList: List<String> = emptyList(),
    // 사진 위 태그들
    val productTagPinList : List<Tag> = emptyList(),
    // 저장됨 수
    val savedCount: Int = 0,
    // 댓글 수
    val commentCount: Int = 0,
    // 랭킹 정보 (1, 2, 3등) 추가
    val rank: Int? = null

    // val createAt: Long = System.currentMillis(), 게시글 작성일시
    // isDelete: Boolean = false
    // val createAt: Long = System.currentMillis()
)
