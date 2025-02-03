package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialCarouselAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialDiscoveryViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentSocialDetailBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SocialDetailFragment : Fragment() {

    private var _binding: FragmentSocialDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext: Context

    // Firebase Firestore와 FirebaseAuth 초기화
    private val firestore = FirebaseFirestore.getInstance()

    // Fragment간 통신방법 : 뷰모델 공유
    private val viewModel: SocialDiscoveryViewModel by activityViewModels()

    // 현재 로그인 유저가 게시글 작성자를 팔로잉 하는지에 대한 상태
    private var isFollowing = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    // 프래그먼트의 뷰를 생성하는 메서드
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 초기화 작업을 수행하는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.selectedPost.observe(viewLifecycleOwner) { post ->

            // 현재 사용자의 팔로잉 목록을 가져와서 작성자 ID가 있는지 확인
                firestore.collection("Member").document(appContext.getUserId())
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                    val currentUser = documentSnapshot.toObject(Member::class.java)
                    val followingList = currentUser?.followingList ?: emptyList()

                    // 팔로잉 목록에 게시글 작성자의 memberDocId가 있으면 isFollowing을 true로 설정
                    isFollowing = followingList.any { it.id == post.memberDocId }
                    updateButtonState(isFollowing)
                }

//             태그 해시맵 데이터를 태그 객체로 바꾸기
//            firestore.collection("Post").document(post.id).collection("productTagPinList")
//                .get()
//                .addOnSuccessListener { tagMap ->
//                    postTagPinList = tagMap?.toObjects(Tag::class.java)
//                }

            setupListeners(post)
            setUpTextUI(post)
            setUpImageUI(post)
            // displayTags(post)
        }

//      Fragment간 통신방법 : Safe Args
//      SocialDetailFragment에서 데이터 받기
//        val post = arguments?.let { SocialDetailFragmentArgs.fromBundle(it).post }
//        post?.let { setUpUI(it) }
//        Log.d("test909", "소셜 디테일 프래그먼트 : ${post}")

//        Fragment Manager 통신 방법 : Fragment Manager 사용
//        setFragmentResult는 같은 FragmentManager 내에서만 동작하므로 Navigation으로 이동 시 사용 불가능
//        setFragmentResultListener("clickedPost") { clickedPost, bundle ->
//            val clickedId = bundle.getString("id")
//            val clickedNickname = bundle.getString("nickname")
//            val clickedAuthorProfilePicture = bundle.getString("authorProfilePicture")
//            val clickedTitle = bundle.getString("title")
//            val clickedContent = bundle.getString("content")
//            val clickedImageList = bundle.getStringArrayList("imageList")
//            val clickedSavedCount = bundle.getInt("savedCount")
//            val clickedCommentCount = bundle.getInt("commentCount")
//
//            // Post 객체 생성
//            val post = Post(
//                id = clickedId.orEmpty(),
//                nickname = clickedNickname.orEmpty(),
//                authorProfilePicture = clickedAuthorProfilePicture.orEmpty(),
//                title = clickedTitle.orEmpty(),
//                content = clickedContent.orEmpty(),
//                imageList = clickedImageList?.toList() ?: emptyList(),
//                productTagPinList = emptyList(),
//                savedCount = clickedSavedCount,
//                commentCount = clickedCommentCount
//            )
//
//            Log.d("test909", "소셜 디테일 프래그먼트 : ${post}")
//            setUpUI(post)
//        }
    }

    // 프래그먼트가 파괴될 때 Binding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpTextUI(post: Post){
        with(binding){
            tvSocialDetailContent.text = post.content
            tvSocialDetailLikeCount.text = post.savedCount.toString()
            tvSocialDetailCommentCount.text = post.commentCount.toString()
            tvSocialDetailPostItemTitle.text = post.title
            tvSocialDetailContent.text = post.content
            tvSocialDetailPostItemNickname.text = post.nickname
        }
    }


    private fun setUpImageUI(post: Post){
        with(binding){
            // 게시글 이미지 캐러셀
            val photos = post.imageList.map { imageUrl ->
                Uri.parse(imageUrl) // String -> Uri 변환
            }
            val adapter = SocialCarouselAdapter(photos) { position, x, y ->
            }
            vpSocialDetailPictureCarousel.adapter = adapter

            // 작성자 프로필 사진
            Glide.with(this@SocialDetailFragment)
                .load(post.authorProfilePicture) // URL 로드
                .into(ivSocialDetailPostItemProfilePicture) // ImageView에 적용
        }
    }


    private fun displayTags(post: Post) {
        val tagPinContainer = binding.flSocialDetailTagPinOverlay
        Log.d("TagDebug", "productTagPinList Type: ${post.productTagPinList.javaClass.name}")
        Log.d("TagDebug", "First item: ${post.productTagPinList[0]}")
        Log.d("TagDebug", "First item type: ${post.productTagPinList[0].javaClass.name}")

        post.productTagPinList.forEach { tag ->

            val x = tag.tagX!!.toFloat()
            val y = tag.tagY!!.toFloat()

            // 태그 핀 이미지 생성
            val tagPinImageView = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_tag_pin)  // 태그 핀 이미지 리소스
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = (x - 20).toInt()
                    topMargin = (y - 20).toInt()
                }
            }
            // Container에 추가
            tagPinContainer.addView(tagPinImageView)
        }
    }


    // 리스너 설정
    private fun setupListeners(post: Post) {
        val selectedMemberId = firestore.collection("Member").document(post.memberDocId) // 게시글 작성자 참조

        with(binding) {
            toolbarSocial.setNavigationOnClickListener {
                findNavController().popBackStack(R.id.navigation_social, false)
            }

            // 로그인한 유저와 게시글 작성자가 동일한지 확인
            if (appContext.getUserId() == post.memberDocId) {
                // 팔로잉 버튼을 숨김
                btnSocialDetailFollowingFollowing.visibility = View.GONE
            } else {
                // 팔로잉 버튼을 보이게 설정
                btnSocialDetailFollowingFollowing.visibility = View.VISIBLE
            }

            // 초기 상태 설정 (isFollowing 여부에 따라)
            updateButtonState(isFollowing)

            // 버튼 클릭 리스너
            btnSocialDetailFollowingFollowing.setOnClickListener {
                isFollowing = !isFollowing
                updateButtonState(isFollowing)

                if (isFollowing) {
                    // 팔로우 목록에 멤버 추가
                    firestore.collection("Member").document(appContext.getUserId())
                        .update("followingList", FieldValue.arrayUnion(selectedMemberId))
                        .addOnSuccessListener {
                            // 팔로잉 카운트와 팔로워 카운트 증가
                            updateFollowCounts(post.memberDocId, increment = true)
                        }
                } else {
                    // 팔로우 목록에서 멤버 삭제
                    firestore.collection("Member").document(appContext.getUserId())
                        .update("followingList", FieldValue.arrayRemove(selectedMemberId))
                        .addOnSuccessListener {
                            // 팔로잉 카운트와 팔로워 카운트 감소
                            updateFollowCounts(post.memberDocId, increment = false)
                        }
                }
            }
        }
    }

    // 팔로우/언팔로우 시 팔로잉 카운트와 팔로워 카운트를 업데이트
    private fun updateFollowCounts(memberDocId: String, increment: Boolean) {
        val change = if (increment) 1 else -1

        // 나의 followingCount 증가/감소
        firestore.collection("Member").document(appContext.getUserId())
            .update("followingCount", FieldValue.increment(change.toLong()))

        // 유저 A의 followerCount 증가/감소
        firestore.collection("Member").document(memberDocId)
            .update("followerCount", FieldValue.increment(change.toLong()))
    }


    // 팔로잉 토글 효과
    private fun updateButtonState(isFollowing: Boolean) {
        if (isFollowing) {
            binding.btnSocialDetailFollowingFollowing.text = "팔로잉"
            binding.btnSocialDetailFollowingFollowing.setTextColor(Color.parseColor("#58443B")) // 글자색
            binding.btnSocialDetailFollowingFollowing.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#DFDFDF")) // 버튼 배경색
            )
            binding.btnSocialDetailFollowingFollowing.strokeColor =
                ColorStateList.valueOf(Color.parseColor("#818080")) // 테두리 색상
            binding.btnSocialDetailFollowingFollowing.strokeWidth = 1 // 테두리 두께 (px 단위)
            binding.btnSocialDetailFollowingFollowing.setPadding(0,0,0,0)
        } else {
            binding.btnSocialDetailFollowingFollowing.text = "팔로우"
            binding.btnSocialDetailFollowingFollowing.setTextColor(Color.parseColor("#FFFFFF")) // 글자색
            binding.btnSocialDetailFollowingFollowing.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#332828")) // 버튼 배경색
            )
            binding.btnSocialDetailFollowingFollowing.strokeColor =
                ColorStateList.valueOf(Color.parseColor("#000000")) // 테두리 색상 (기본값, 변경 가능)
            binding.btnSocialDetailFollowingFollowing.strokeWidth = 1 // 테두리 두께 (px 단위)
            binding.btnSocialDetailFollowingFollowing.setPadding(0,0,0,0)
        }
    }
}