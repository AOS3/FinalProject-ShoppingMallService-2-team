package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.databinding.ItemProductBinding
import com.nemodream.bangkkujaengi.utils.getUserType
import com.nemodream.bangkkujaengi.utils.loadImage
import com.nemodream.bangkkujaengi.utils.toCommaString

class SearchResultAdapter(
    private val productClickListener: ProductClickListener,
) : ListAdapter<Product, SearchResultAdapter.SearchResultViewHolder>(
    SearchResultDiffCallBack()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            onProductClickListener = { position -> productClickListener.onProductClick(getItem(position)) },
            onFavoriteClickListener = { position -> productClickListener.onFavoriteClick(getItem(position)) },
        )
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SearchResultViewHolder(
        private val binding: ItemProductBinding,
        private val onProductClickListener: (position: Int) -> Unit,
        private val onFavoriteClickListener: (position: Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onProductClickListener(adapterPosition)
            }
            binding.btnLike.setOnClickListener {
                onFavoriteClickListener(adapterPosition)
            }
        }

        fun bind(product: Product) {
            with(binding) {
                ivProduct.loadImage(product.images.first())
                tvProductName.text = product.productName
                tvProductDiscount.text = "${product.saleRate}%"

                when (product.saleRate > 0) {
                    true -> {
                        // 할인율이 0% 초과일 때: 원가는 취소선, 할인가격 표시
                        tvProductPrice.paintFlags =
                            tvProductPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        tvProductDiscount.visibility = View.VISIBLE
                        tvProductDiscountPrice.visibility = View.VISIBLE

                        viewDiscountBlind.visibility = if (root.context.getUserType() == "member") View.GONE else View.VISIBLE
                    }

                    false -> {
                        // 할인율이 0%일 때: 원가만 표시 (취소선 없이)
                        tvProductPrice.paintFlags =
                            tvProductPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        tvProductDiscount.visibility = View.GONE
                        tvProductDiscountPrice.visibility = View.GONE
                    }

                }
                tvProductPrice.text = "${product.price.toCommaString()}원"
                tvProductDiscountPrice.text = "${product.saledPrice.toCommaString()}원"
                tvProductCategory.text = product.category.getTabTitle()
                btnLike.isSelected = product.like
            }
        }
    }
}

class SearchResultDiffCallBack : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }

}