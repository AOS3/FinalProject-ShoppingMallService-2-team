package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentMyReviewWriteBinding
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.MyReviewWriteViewModel
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.loadImage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyReviewWriteFragment : Fragment() {

    private lateinit var binding: FragmentMyReviewWriteBinding
    private val viewModel: MyReviewWriteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyReviewWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupStarRating()
        setupReviewContentInput()
        loadProductData()
        observeViewModel()
        setupSubmitButton()
    }

    private fun setupToolbar() {
        binding.toolbarReviewWrite.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupStarRating() {
        val stars = listOf(
            binding.star1, binding.star2, binding.star3,
            binding.star4, binding.star5
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                viewModel.selectRating(index + 1)
            }
        }

        // 별점 변화 관찰 및 UI 업데이트
        viewModel.rating.observe(viewLifecycleOwner) { rating ->
            stars.forEachIndexed { index, imageView ->
                if (index < rating) {
                    imageView.setImageResource(R.drawable.ic_star_fill)
                } else {
                    imageView.setImageResource(R.drawable.ic_star_outline)
                }
            }
        }
    }

    private fun setupReviewContentInput() {
        // 리뷰 입력란의 텍스트 변화 감지
        binding.etReviewContent.editText?.addTextChangedListener { text ->
            viewModel.updateReviewContent(text.toString())
        }
    }

    private fun loadProductData() {
        // arguments에서 전달받은 productId를 가져와서 ViewModel에 전달
        val productId = arguments?.getString("productId") ?: return
        viewModel.loadProductData(productId)
    }

    private fun observeViewModel() {
        // 상품명 업데이트
        viewModel.productName.observe(viewLifecycleOwner) { productName ->
            binding.tvReviewProductName.text = productName
        }

        // 상품 이미지 업데이트
        viewModel.productImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            binding.imgReviewProduct.loadImage(imageUrl)  // 확장 함수 사용
        }

        viewModel.isSubmitting.observe(viewLifecycleOwner) { isSubmitting ->
            binding.btnReviewSubmit.isEnabled = !isSubmitting  // 저장 중일 때 비활성화
        }

        viewModel.isSubmitEnabled.observe(viewLifecycleOwner) { isEnabled ->
            // 저장 중이 아닌 경우에만 isSubmitEnabled에 따라 상태 변경
            if (viewModel.isSubmitting.value == false) {
                binding.btnReviewSubmit.isEnabled = isEnabled
            }
        }

        viewModel.reviewSubmitResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(requireContext(), "리뷰가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                binding.btnReviewSubmit.isEnabled = false  // 저장 성공 후 비활성화 유지
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "리뷰 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnReviewSubmit.setOnClickListener {
            val productId = arguments?.getString("productId") ?: return@setOnClickListener
            val documentId = requireContext().getUserId()  // 문서 ID 가져오기
            viewModel.submitReview(productId, documentId)
        }
    }
}
