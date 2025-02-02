package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Review
import com.nemodream.bangkkujaengi.databinding.RowProductDetailReviewBinding

class ProductReviewAdapter : RecyclerView.Adapter<ProductReviewAdapter.ReviewViewHolder>() {

    private val reviews = mutableListOf<Review>()

    fun submitList(newReviews: List<Review>) {
        reviews.clear()
        reviews.addAll(newReviews)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = RowProductDetailReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    inner class ReviewViewHolder(private val binding: RowProductDetailReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvRowNickname.text = review.memberId  // 임시 값, 닉네임으로 갱신 예정
            binding.tvRowContent.text = review.content
            binding.textView3.text = review.reviewDate

            // 별점 설정
            val stars = listOf(
                binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
            )
            for (i in stars.indices) {
                stars[i].setImageResource(
                    if (i < review.rating) R.drawable.ic_star_fill else R.drawable.ic_star_outline
                )
            }
        }
    }
}
