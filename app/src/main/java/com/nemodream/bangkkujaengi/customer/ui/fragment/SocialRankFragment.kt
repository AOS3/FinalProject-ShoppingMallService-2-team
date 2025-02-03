package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.customer.ui.adapter.OnPostItemClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialDiscoveryAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialDiscoveryViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialRankViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentSocialRankBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SocialRankFragment : Fragment(), OnPostItemClickListener {

    private var _binding: FragmentSocialRankBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SocialRankViewModel by viewModels()
    private val shareViewModel: SocialDiscoveryViewModel by activityViewModels()

    private val socialRankAdapter: SocialDiscoveryAdapter by lazy {
        SocialDiscoveryAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialRankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        with(binding.rvSocialRank) {
            layoutManager = GridLayoutManager(context, 2) // 한 행에 2개의 열
            adapter = socialRankAdapter
        }
    }

    /**
     * ViewModel에서 데이터를 관찰하고 UI를 업데이트
     */
    private fun observeViewModel() {
        // 게시글 목록을 뷰모델에서 관찰
        viewModel.posts.observe(viewLifecycleOwner, Observer { posts ->
            // 상위 3개 포스트에만 rank 필드를 추가
            val rankedPosts = posts.mapIndexed { index, post ->
                if (index < 3) {
                    post.copy(rank = index + 1) // 1, 2, 3위만 rank 필드 설정
                } else {
                    post.copy(rank = null) // 나머지 null 처리
                }
            }
            socialRankAdapter.submitList(rankedPosts)
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
