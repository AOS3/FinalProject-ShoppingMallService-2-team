package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialFollowingProfilesAdapter
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialDiscoveryAdapter
import com.nemodream.bangkkujaengi.customer.ui.adapter.OnPostItemClickListener
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialFollowingViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentSocialFollowingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocialFollowingFragment : Fragment(), OnPostItemClickListener {

    private var _binding: FragmentSocialFollowingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SocialFollowingViewModel by viewModels()

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
        viewModel.loadFollowingMembers() // 팔로잉 멤버 데이터를 로드
    }

    // 프래그먼트가 파괴될 때 Binding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 뷰페이저 사용 시 인스턴스를 생성하기 위한 메서드
    companion object {
        fun newInstance(): SocialFollowingFragment {
            return SocialFollowingFragment()
        }
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
                profileAdapter.setSelectedMemberId(it.id) // 어댑터에 선택된 멤버 ID 설정
            }
        }

        // 선택된 멤버의 게시글 리스트 관찰
        viewModel.memberPosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts) // 게시글 리스트를 어댑터에 업데이트
        }
    }

    /**
     * 게시글 클릭 이벤트를 처리하는 메서드
     */
    override fun onPostItemClick(post: Post) {
        // 게시글 클릭 시 처리할 로직 (예: 상세 화면으로 이동)
    }
}
