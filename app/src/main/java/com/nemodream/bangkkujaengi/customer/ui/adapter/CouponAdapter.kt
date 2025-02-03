package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import com.nemodream.bangkkujaengi.databinding.ItemCouponListBinding
import com.nemodream.bangkkujaengi.utils.toCommaString
import com.nemodream.bangkkujaengi.utils.toFormattedDate

class CouponAdapter(
    private val couponReceiveClickListener: CouponReceiveClickListener,
): ListAdapter<Coupon, CouponAdapter.CouponViewHolder>(CouponDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        return CouponViewHolder(
            ItemCouponListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onCouponReceiveClick = { position -> couponReceiveClickListener.onCouponReceiveClick(getItem(position)) }
        )
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CouponViewHolder(
        private val binding: ItemCouponListBinding,
        private val onCouponReceiveClick: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnCouponReceive.setOnClickListener {
                onCouponReceiveClick(adapterPosition)
            }
        }

        fun bind(coupon: Coupon) {
            with(binding) {
                tvCouponTitle.text = coupon.title
                tvCouponDiscount.text =
                    if (coupon.couponType == "SALE_RATE") "${coupon.saleRate} %" else "${coupon.salePrice.toCommaString()} 원"
                tvCouponDescription.text = coupon.conditionDescription
                tvCouponLimitDate.text = "~ ${coupon.endCouponDate?.toFormattedDate()}"
                btnCouponReceive.visibility = if (coupon.isHold) View.INVISIBLE else View.VISIBLE
            }
        }
    }
}

class CouponDiffCallback : DiffUtil.ItemCallback<Coupon>() {
    override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
        return oldItem.startCouponDate == newItem.startCouponDate
    }

    override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
        return oldItem == newItem
    }
}

interface CouponReceiveClickListener {
    fun onCouponReceiveClick(coupon: Coupon)
}