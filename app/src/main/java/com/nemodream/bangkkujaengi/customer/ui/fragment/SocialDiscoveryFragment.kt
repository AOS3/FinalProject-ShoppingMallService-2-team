package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.util.Log
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
import com.nemodream.bangkkujaengi.databinding.FragmentSocialDiscoveryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocialDiscoveryFragment : Fragment(), OnPostItemClickListener {

    private var _binding: FragmentSocialDiscoveryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SocialDiscoveryViewModel by activityViewModels()

    private val socialDiscoveryAdapter: SocialDiscoveryAdapter by lazy {
        SocialDiscoveryAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialDiscoveryBinding.inflate(inflater, container, false)
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
        with(binding.rvSocialDiscovery) {
            layoutManager = GridLayoutManager(context, 2) // 한 행에 2개의 열
            adapter = socialDiscoveryAdapter
        }
    }

    /**
     * ViewModel에서 데이터를 관찰하고 UI를 업데이트
     */
    private fun observeViewModel() {
        // 게시글 목록을 뷰모델에서 관찰
        viewModel.posts.observe(viewLifecycleOwner, Observer { posts ->
            socialDiscoveryAdapter.submitList(posts)
        })
    }

    /**
     * 게시글 클릭 이벤트 처리
     */
    override fun onPostItemClick(post: Post) {
        Log.d("SocialDiscoveryFragment", "Post clicked: ${post}")

//        Fragment간 통신방법 : Fragment Manager 사용
//        setFragmentResult는 같은 FragmentManager 내에서만 동작하므로 Navigation으로 이동 시 사용 불가능
//        val clickedPost = Bundle().apply {
//            putString("id", post.id)
//            putString("nickname", post.id)
//            putString("authorProfilePicture", post.authorProfilePicture)
//            putString("title", post.title)
//            putString("content", post.content)
//            putStringArrayList("imageList", post.imageList as ArrayList<String?>?)
//            // putStringArrayList("productTagPinList", post.productTagPinList as ArrayList<String?>?)
//            putInt("savedCount", post.savedCount)
//            putInt("commentCount", post.commentCount)
//        }
//        Log.d("test909","소셜 디스커버리 프래그먼트 : ${clickedPost}")
//        setFragmentResult("clickedPost", clickedPost)

        // Fragment간 통신방법 : Safe Args 사용
        // SocialDiscoveryFragment에서 Safe Args로 데이터 전달

        // Fragment간 통신방법 : 뷰모델 공유
        viewModel.selectedPost.value = post
        Log.d("test909","소셜 디스커버리 프래그먼트 : ${viewModel.selectedPost.value}")
        val action = SocialFragmentDirections.actionSocialFragmentToSocialDetailFragment()
        findNavController().navigate(action)
    }
}
