package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.data.model.SocialLogin
import com.nemodream.bangkkujaengi.customer.ui.adapter.OnPostItemClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialDiscoveryAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialDiscoveryViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialMyViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentSocialMyBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.loadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SocialMyFragment : Fragment(), OnPostItemClickListener {

    private var _binding: FragmentSocialMyBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext: Context

    private val viewModel: SocialMyViewModel by viewModels()
    private val shareViewModel: SocialDiscoveryViewModel by activityViewModels()
    private val socialMyAdapter: SocialDiscoveryAdapter by lazy { SocialDiscoveryAdapter(this) }

    private var isMyPostsSelected: Boolean = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 초기설정
        updateTabStyle(isMyPostsSelected = true)
        viewModel.loadMyWrittenPosts(appContext.getUserId())

        setupRecyclerView()
        observeViewModel()
        viewModel.loadMyProfile(appContext.getUserId()) // 프로필 데이터 로드
        setupTabClickListeners() // 탭 클릭 리스너 설정
        setupProfileEdit() // 편집 모드 전환
    }

    override fun onPause() {
        super.onPause()
        resetToNormalMode() // 다른 프래그먼트로 이동 시 일반 모드로 초기화
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 수정 모드에서 일반 모드로 전환
    private fun resetToNormalMode() {
        val nicknameEditText = binding.etMyProfileNickname
        val nicknameTextView = binding.tvMyProfileNickname
        // val editIcon = binding.ivSocialMyEdit
        val saveButton = binding.btnSocialMySaveEdit

        nicknameEditText.visibility = View.GONE // EditText 숨기기
        nicknameTextView.visibility = View.VISIBLE // TextView 보이기
        // editIcon.visibility = View.VISIBLE // 수정 아이콘 다시 표시
        saveButton.visibility = View.GONE  // "수정완료" 버튼 숨김
    }

    private fun setupProfileEdit() {
        val nicknameEditText = binding.etMyProfileNickname // EditText로 변경된 닉네임 필드
        val nicknameTextView = binding.tvMyProfileNickname // 원래 TextView
        // val editIcon = binding.ivSocialMyEdit
        val saveButton = binding.btnSocialMySaveEdit

        // 초기 상태: 닉네임 편집 불가능
        nicknameEditText.visibility = View.GONE // EditText 숨기기
        nicknameTextView.visibility = View.VISIBLE // TextView 보이기

        // 수정 버튼 클릭
//        editIcon.setOnClickListener {
//            nicknameEditText.setText(nicknameTextView.text) // TextView의 텍스트를 EditText로 복사
//            nicknameTextView.visibility = View.GONE // TextView 숨기기
//            nicknameEditText.visibility = View.VISIBLE // EditText 보이기
//            nicknameEditText.requestFocus()  // 포커스 이동
//            editIcon.visibility = View.GONE  // 수정 아이콘 숨김
//            saveButton.visibility = View.VISIBLE // "수정완료" 버튼 표시
//            binding.btnSocialMySaveEdit.setPadding(0,0,0,0)
//            binding.etMyProfileNickname.setPadding(8,0,0,20)
//        }

        // "수정완료" 버튼 클릭
        saveButton.setOnClickListener {
            val editedNickname = nicknameEditText.text.toString() // 수정된 닉네임
            viewModel.updateNickname(editedNickname) // ViewModel에 저장 로직 호출

            nicknameTextView.text = editedNickname // TextView에 수정된 텍스트 표시
            nicknameEditText.visibility = View.GONE // EditText 숨기기
            nicknameTextView.visibility = View.VISIBLE // TextView 보이기
            //editIcon.visibility = View.VISIBLE // 수정 아이콘 다시 표시
            saveButton.visibility = View.GONE  // "수정완료" 버튼 숨김
        }
    }

    // 내가쓴글, 저장됨 탭 클릭 리스너
    private fun setupTabClickListeners() {
        // 내가쓴글 탭 클릭
        binding.tvMyPosts.setOnClickListener {
            isMyPostsSelected = true
            viewModel.loadMyWrittenPosts(appContext.getUserId())
            updateTabStyle(isMyPostsSelected)
        }
        // 저장됨 탭 클릭
        binding.tvSavedPosts.setOnClickListener {
            isMyPostsSelected = false
            viewModel.loadMySavedPosts()
            updateTabStyle(isMyPostsSelected)
        }
    }

    // 탭 스타일 업데이트
    private fun updateTabStyle(isMyPostsSelected: Boolean) {
        // "내가쓴글" 선택시
        binding.tvMyPosts.setTypeface(null, if (isMyPostsSelected) Typeface.BOLD else Typeface.NORMAL)
        // "저장됨" 선택시
        binding.tvSavedPosts.setTypeface(null, if (isMyPostsSelected) Typeface.NORMAL else Typeface.BOLD)
    }


    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        with(binding.rvMyPosts) {
            layoutManager = GridLayoutManager(context, 2) // 한 행에 2개의 열
            adapter = socialMyAdapter
        }
    }

    /**
     * ViewModel에서 데이터를 관찰하고 UI를 업데이트
     */
    private fun observeViewModel() {
        // 프로필 데이터 관찰
        viewModel.myProfile.observe(viewLifecycleOwner, Observer { profile ->
            with(binding) {
                profile!!.memberProfileImage?.let {
                    ivMyProfileImage.loadImage(profile.memberProfileImage.toString())
                } ?: ivMyProfileImage.setImageResource(R.drawable.ic_default_profile)
                tvMyProfileNickname.text = "${profile.memberNickName}"
                tvMyProfileFollowInfo.text = "팔로잉 ${profile.followingList.size}명   팔로워 ${profile.followerCount}명"
            }
        })


        // 게시글 데이터 관찰
        viewModel.posts.observe(viewLifecycleOwner, Observer { posts ->
            // postAdapter.submitList(posts) // 게시글 리스트를 어댑터에 업데이트
            if (posts.isNullOrEmpty()) {
                binding.tvNoPosts.visibility = View.VISIBLE
                binding.rvMyPosts.visibility = View.GONE
                // "내가쓴글" 또는 "저장됨" 탭에 따라 다른 메시지 표시
                // "내가쓴글" 또는 "저장됨" 탭에 따라 다른 메시지 표시
                binding.tvNoPosts.text = if (isMyPostsSelected) {
                    "작성한 게시글이 없습니다"
                } else {
                    "저장한 게시글이 없습니다"
                }
            } else {
                binding.tvNoPosts.visibility = View.GONE
                binding.rvMyPosts.visibility = View.VISIBLE
                socialMyAdapter.submitList(posts)
            }
        })
    }

    /**
     * 게시글 클릭 이벤트 처리
     */
    override fun onPostItemClick(post: Post) {
        shareViewModel.selectedPost.value = post
        val action = SocialFragmentDirections.actionSocialFragmentToSocialDetailFragment()
        findNavController().navigate(action)
    }
}
