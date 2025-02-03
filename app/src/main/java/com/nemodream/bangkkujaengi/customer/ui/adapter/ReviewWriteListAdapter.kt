package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseItem
import com.nemodream.bangkkujaengi.databinding.RowReviewWriteBinding
import com.nemodream.bangkkujaengi.utils.loadImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



class ReviewWriteListAdapter(private val onClick: (String) -> Unit) :
    ListAdapter<PurchaseItem, ReviewWriteListAdapter.ReviewViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = RowReviewWriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val reviewItem = getItem(position)
        holder.bind(reviewItem)
    }

    inner class ReviewViewHolder(private val binding: RowReviewWriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reviewItem: PurchaseItem) {
            // 상품명 설정
            binding.tvReviewWriteProductName.text = reviewItem.productTitle

            // 작성 만료일 계산 후 설정
            val daysLeftText = calculateDaysLeft(reviewItem.purchaseConfirmedDate)
            binding.tvReviewWriteDeadlineInfo.text = daysLeftText

            // 확장 함수로 이미지 로드
            binding.imgReviewWriteProduct.loadImage(reviewItem.imageUrl)

            binding.btnWriteReview.setOnClickListener {
                onClick(reviewItem.productId)
            }
        }
    }


    // 날짜 계산 함수
    private fun calculateDaysLeft(purchaseDateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val purchaseDate = dateFormat.parse(purchaseDateString)
            val calendar = Calendar.getInstance().apply {
                time = purchaseDate!!
                add(Calendar.DAY_OF_MONTH, 30)
            }

            val currentDate = Calendar.getInstance().time
            val daysLeft = ((calendar.time.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()

            if (daysLeft > 0) {
                "작성 만료 D-$daysLeft"
            } else {
                "작성 기한 만료"
            }
        } catch (e: Exception) {
            "날짜 정보 없음"
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<PurchaseItem>() {
            override fun areItemsTheSame(oldItem: PurchaseItem, newItem: PurchaseItem): Boolean =
                oldItem.productTitle == newItem.productTitle

            override fun areContentsTheSame(oldItem: PurchaseItem, newItem: PurchaseItem): Boolean =
                oldItem == newItem
        }
    }
}
