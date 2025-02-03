package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Review
import com.nemodream.bangkkujaengi.databinding.RowReviewWrittenBinding
import com.nemodream.bangkkujaengi.utils.loadImage

class ReviewWrittenListAdapter :
    ListAdapter<Review, ReviewWrittenListAdapter.ViewHolder>(WrittenReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowReviewWrittenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: RowReviewWrittenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Review) {
            // 상품명, 리뷰 내용, 작성일 설정
            binding.tvReviewWrittenProductName.text = item.productTitle
            binding.tvReviewWrittenContent.text = item.content
            binding.tvReviewWrittenDate.text = "작성일: ${item.reviewDate}"

            // 적립금 상태 (필요에 따라 업데이트)
            binding.tvReviewWrittenRewardStatus.text = if (!item.isDelete) "적립금 지급 완료" else "리뷰 삭제됨"

            // 상품 이미지 로드
            binding.imgReviewWrittenProduct.loadImage(item.productImageUrl)

            // 별점 설정
            val stars = listOf(
                binding.star1, binding.star2, binding.star3,
                binding.star4, binding.star5
            )
            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < item.rating) R.drawable.ic_star_fill else R.drawable.ic_star_outline
                )
            }
        }
    }
}

class WrittenReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
    override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
        return oldItem == newItem
    }
}
