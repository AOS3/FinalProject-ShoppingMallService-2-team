package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.PromotionHeader
import com.nemodream.bangkkujaengi.customer.data.model.PromotionItem
import com.nemodream.bangkkujaengi.customer.data.model.PromotionProducts
import com.nemodream.bangkkujaengi.databinding.ItemPromotionHeaderBinding
import com.nemodream.bangkkujaengi.databinding.ItemPromotionProductsBinding

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_PRODUCTS = 1

class PromotionAdapter(
    private val productClickListener: ProductClickListener,
    private val moreProductsClickListener: MoreProductsClickListener,
    ) : ListAdapter<PromotionItem, RecyclerView.ViewHolder>(PromotionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                PromotionHeaderViewHolder(
                    ItemPromotionHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    moreProductsClickListener,
                )
            }
            else -> {
                PromotionProductsViewHolder(
                    ItemPromotionProductsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    productClickListener
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PromotionHeaderViewHolder -> {
                val item = getItem(position) as PromotionHeader
                holder.bind(item)
            }

            is PromotionProductsViewHolder -> {
                val item = getItem(position) as PromotionProducts
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PromotionHeader -> VIEW_TYPE_HEADER
            is PromotionProducts -> VIEW_TYPE_PRODUCTS
        }
    }

    class PromotionHeaderViewHolder(
        private val binding: ItemPromotionHeaderBinding,
        moreProductsClickListener: MoreProductsClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvMoreProducts.setOnClickListener {
                moreProductsClickListener.onMoreProductsClick(binding.tvTitle.text.toString())
            }
        }

        fun bind(item: PromotionHeader) {
            binding.tvMoreProducts.paintFlags =
                binding.tvMoreProducts.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.tvTitle.text = item.title
        }
    }

    class PromotionProductsViewHolder(
        binding: ItemPromotionProductsBinding,
        productClickListener: ProductClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val productAdapter = PromotionProductAdapter(productClickListener)

        init {
            binding.rvProducts.adapter = productAdapter
        }

        fun bind(item: PromotionProducts) {
            productAdapter.submitList(item.products)
        }
    }
}

class PromotionDiffCallback : DiffUtil.ItemCallback<PromotionItem>() {
    override fun areItemsTheSame(oldItem: PromotionItem, newItem: PromotionItem): Boolean {
        return when {
            oldItem is PromotionHeader && newItem is PromotionHeader ->
                oldItem.title == newItem.title

            oldItem is PromotionProducts && newItem is PromotionProducts ->
                oldItem.products.first().productId == newItem.products.first().productId

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: PromotionItem, newItem: PromotionItem): Boolean {
        return oldItem == newItem
    }
}

interface MoreProductsClickListener {
    fun onMoreProductsClick(title: String)
}