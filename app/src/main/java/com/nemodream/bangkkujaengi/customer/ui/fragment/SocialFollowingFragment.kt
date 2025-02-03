package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.data.repository.SocialFollowingRepository
import com.nemodream.bangkkujaengi.customer.ui.adapter.OnPostItemClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialDiscoveryAdapter
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialFollowingProfilesAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialDiscoveryViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialFollowingViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentSocialFollowingBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.loadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SocialFollowingFragment : Fragment(), OnPostItemClickListener {

    private var _binding: FragmentSocialFollowingBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext: Context

    private val viewModel: SocialFollowingViewModel by viewModels()
    private val shareViewModel: SocialDiscoveryViewModel by activityViewModels()

    // 팔로잉 프로필 RecyclerView의 어댑터
    private val profileAdapter: SocialFollowingProfilesAdapter by lazy {
        SocialFollowingProfilesAdapter(mutableListOf()) { member ->
            viewModel.selectMember(member) // 클릭된 멤버를 ViewModel에 알림
        }
    }

    // 게시글 RecyclerView의 어댑터
    private val postAdapter: SocialDiscoveryAdapter by lazy {
        SocialDiscoveryAdapter(this) // Fragment 자체가 OnPostItemClickListener를 구현
    }

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
        _binding = FragmentSocialFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 초기화 작업을 수행하는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews() // RecyclerView 초기화
        observeViewModel() // ViewModel 데이터 관찰 설정
        viewModel.loadFollowingMembers(appContext.getUserId()) // 팔로잉 멤버 데이터를 로드
        setupListeners() // 리스너 설정
    }

    // 프래그먼트가 파괴될 때 Binding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // RecyclerView를 초기화하는 메서드
    private fun setupRecyclerViews() {
        // 팔로잉 프로필 RecyclerView 초기화
        binding.rvFollowingProfiles.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) // 수평 스크롤 설정
            adapter = profileAdapter
        }

        // 게시글 RecyclerView 초기화
        binding.rvFollowingPosts.apply {
            layoutManager = GridLayoutManager(context, 2) // 2열 그리드 레이아웃 설정
            adapter = postAdapter
        }
    }

    // ViewModel 데이터를 관찰하고 UI를 업데이트하는 메서드
    private fun observeViewModel() {
        // 팔로잉 멤버 리스트 관찰
        viewModel.followingMembers.observe(viewLifecycleOwner) { members ->
            profileAdapter.updateList(members) // 멤버 리스트를 어댑터에 업데이트
        }

        // 선택된 멤버 관찰
        viewModel.selectedMember.observe(viewLifecycleOwner) { selectedMember ->
            selectedMember?.let {
                // 선택된 팔로잉의 프로필 아이콘 테두리, 닉네임 표시
                profileAdapter.setSelectedMemberId(it.id) // 어댑터에 선택된 멤버 ID 설정

                // 선택된 팔로잉의 프로필 정보바 표시
                binding.clSelectedProfileInfo.visibility = View.VISIBLE
                binding.ivSelectedProfileImage.loadImage(selectedMember.memberProfileImage.toString())
                binding.tvSelectedProfileNickname.text = it.memberNickName
                binding.tvSelectedProfileFollowInfo.text =
                    "팔로잉 ${selectedMember.followingList.size}명     팔로워 ${selectedMember.followerCount}명"
            } ?: run {
                // 선택된 멤버가 없을 경우 프로필 정보를 숨김
                binding.clSelectedProfileInfo.visibility = View.GONE
            }
        }

        // 선택된 멤버의 게시글 리스트 관찰
        viewModel.memberPosts.observe(viewLifecycleOwner) { posts ->
            val selectedMember = viewModel.selectedMember.value

            // 게시글 유무에 따라 "게시글이 아직 없습니다" 텍스트 보여줌
            if (selectedMember != null && posts.isNullOrEmpty()) {
                binding.tvNoPosts.visibility = View.VISIBLE
                binding.rvFollowingPosts.visibility = View.GONE
            } else {
                binding.tvNoPosts.visibility = View.GONE
                binding.rvFollowingPosts.visibility = View.VISIBLE
                postAdapter.submitList(posts) // 게시글 리스트를 어댑터에 업데이트
            }
        }

        // 팔로잉 상태에 따라 버튼 텍스트 변경
        viewModel.isFollowing.observe(viewLifecycleOwner) { isFollowing ->
            updateFollowingButtonStyle(isFollowing)
        }

        // 팔로잉 멤버 리스트 관찰
        viewModel.followingMembers.observe(viewLifecycleOwner) { followingMembers ->
            // 팔로잉 멤버 유무에 따라 "아직 팔로잉한 유저가 없습니다!" 텍스트 보여줌
            if (followingMembers.isNullOrEmpty()) {
                binding.tvNoFollowing.visibility = View.VISIBLE
                binding.tvNoFollowingSub.visibility = View.VISIBLE
                binding.topScrollLayout.visibility = View.GONE
                binding.clSelectedProfileInfo.visibility = View.GONE
                binding.rvFollowingPosts.visibility = View.GONE
                binding.tvNoPosts.visibility = View.GONE
            }
            else{
                binding.tvNoFollowing.visibility = View.GONE
                binding.tvNoFollowingSub.visibility = View.GONE
                binding.topScrollLayout.visibility = View.VISIBLE
                binding.clSelectedProfileInfo.visibility = View.VISIBLE
                binding.rvFollowingPosts.visibility = View.VISIBLE
            }
        }
    }


    // 팔로우/팔로잉 버튼 스타일 업데이트
    private fun updateFollowingButtonStyle(isFollowing: Boolean) {
        if (isFollowing) {
            binding.btnFollowingFollowing.text = "팔로잉"
            binding.btnFollowingFollowing.setTextColor(Color.parseColor("#58443B")) // 글자색
            binding.btnFollowingFollowing.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#DFDFDF")) // 버튼 배경색
            )
            binding.btnFollowingFollowing.strokeColor =
                ColorStateList.valueOf(Color.parseColor("#818080")) // 테두리 색상
            binding.btnFollowingFollowing.strokeWidth = 1 // 테두리 두께
            binding.btnFollowingFollowing.setPadding(0,0,0,0) // 패딩
        } else {
            binding.btnFollowingFollowing.text = "팔로우"
            binding.btnFollowingFollowing.setTextColor(Color.parseColor("#FFFFFF")) // 글자색
            binding.btnFollowingFollowing.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#332828")) // 버튼 배경색
            )
            binding.btnFollowingFollowing.strokeColor =
                ColorStateList.valueOf(Color.parseColor("#000000")) // 테두리 색상
            binding.btnFollowingFollowing.strokeWidth = 1 // 테두리 두께
            binding.btnFollowingFollowing.setPadding(0,0,0,0) // 패딩
        }
    }


    // 리스너 설정
    private fun setupListeners() {
        with(binding) {
            // 팔로잉 상태 토글
            btnFollowingFollowing.setOnClickListener {
                viewModel.toggleFollowing(appContext.getUserId())
            }

            tvAllProfiles.setOnClickListener {
                val action = SocialFragmentDirections.actionNavigationSocialToSocialFollowingAllFragment()
                findNavController().navigate(action)
            }
        }
    }


    //게시글 클릭 이벤트를 처리하는 메서드
    override fun onPostItemClick(post: Post) {
        shareViewModel.selectedPost.value = post
        val action = SocialFragmentDirections.actionSocialFragmentToSocialDetailFragment()
        findNavController().navigate(action)
    }
}
