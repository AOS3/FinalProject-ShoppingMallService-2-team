package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.ui.adapter.ReviewWriteListAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.MyReviewWriteListViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentMyReviewWriteListBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyReviewWriteListFragment : Fragment() {

    private lateinit var binding: FragmentMyReviewWriteListBinding
    private val viewModel: MyReviewWriteListViewModel by viewModels()
    private val reviewListAdapter by lazy {
        ReviewWriteListAdapter { productId ->
            navigateToWriteReviewFragment(productId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyReviewWriteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 리사이클러뷰 설정
        binding.recyclerReviewWrite.apply {
            adapter = reviewListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        observeViewModel()

        // 회원 문서 ID 가져오기
        val documentId = requireContext().getUserId()
        Log.d("FirestoreDebug", "Using document ID: $documentId")
        if (documentId.isNullOrEmpty()) {
            showError("로그인 정보가 없습니다.")
            return
        }

        // 뷰모델에서 데이터 로드
        viewModel.loadPurchases(documentId)
    }

    private fun observeViewModel() {
        viewModel.purchases.observe(viewLifecycleOwner) { purchases ->
            if (purchases.isNullOrEmpty()) {
                showNoReviewsScreen("작성할 리뷰가 없습니다.")
            } else {
                // 어댑터에 데이터 전달
                reviewListAdapter.submitList(purchases)
            }
        }
    }

    private fun navigateToWriteReviewFragment(productId: String) {
        val action = MyReviewFragmentDirections.actionMyReviewWriteListFragmentToMyReviewWriteFragment(productId)
        findNavController().navigate(action)

        Toast.makeText(requireContext(), "$productId 리뷰 작성으로 이동합니다.", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showNoReviewsScreen(message: String) {
        // 화면 전환: RecyclerView 숨기고 No Reviews Layout 표시
        binding.recyclerReviewWrite.visibility = View.GONE

        // No Reviews 화면 추가
        val noReviewsView = layoutInflater.inflate(R.layout.blank, binding.root, false)
        binding.root.addView(noReviewsView)

        // 동적으로 텍스트 변경
        val messageView = noReviewsView.findViewById<TextView>(R.id.tv_blank)
        messageView.text = message
    }
}
