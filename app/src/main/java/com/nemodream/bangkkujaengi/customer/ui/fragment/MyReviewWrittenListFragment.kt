package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.ui.adapter.ReviewWrittenListAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.MyReviewWrittenListViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentMyReviewWrittenListBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyReviewWrittenListFragment : Fragment() {

    private lateinit var binding: FragmentMyReviewWrittenListBinding
    private val viewModel: MyReviewWrittenListViewModel by viewModels()
    private val reviewListAdapter by lazy { ReviewWrittenListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyReviewWrittenListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        loadReviews()
    }

    private fun setupRecyclerView() {
        binding.recyclerReviewWritten.apply {
            adapter = reviewListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.writtenReviews.observe(viewLifecycleOwner) { reviews ->
            if (reviews.isNullOrEmpty()) {
                showNoReviewsScreen("작성한 리뷰가 없습니다.")
            } else {
                // 데이터가 있으면 RecyclerView에 데이터 표시
                reviewListAdapter.submitList(reviews)
            }
        }
    }

    private fun loadReviews() {
        val documentId = requireContext().getUserId()
        if (documentId.isNotEmpty()) {
            viewModel.loadWrittenReviews(documentId)
        } else {
            // 로그인 정보가 없을 때 토스트 메시지 표시
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNoReviewsScreen(message: String) {
        // 화면 전환: RecyclerView 숨기고 No Reviews Layout 표시
        binding.recyclerReviewWritten.visibility = View.GONE

        // No Reviews 화면 추가
        val noReviewsView = layoutInflater.inflate(R.layout.blank, binding.root, false)
        binding.root.addView(noReviewsView)

        // 동적으로 텍스트 변경
        val messageView = noReviewsView.findViewById<TextView>(R.id.tv_blank)
        messageView.text = message
    }
}

