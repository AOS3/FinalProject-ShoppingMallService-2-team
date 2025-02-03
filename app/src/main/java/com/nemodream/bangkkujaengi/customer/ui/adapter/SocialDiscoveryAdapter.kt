package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.databinding.ItemSocialPostBinding
import com.nemodream.bangkkujaengi.utils.loadImage

class SocialDiscoveryAdapter(
    private val listener: OnPostItemClickListener
): RecyclerView.Adapter<SocialDiscoveryAdapter.SocialDiscoveryViewHolder>() {
    private val items = mutableListOf<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialDiscoveryViewHolder {
        val binding = ItemSocialPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SocialDiscoveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialDiscoveryViewHolder, position: Int) {
        holder.bind(items[position], listener)
    }

    fun submitList(socialDiscoveryItems: List<Post>) {
        items.clear()
        items.addAll(socialDiscoveryItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class SocialDiscoveryViewHolder(private val binding: ItemSocialPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post, listener: OnPostItemClickListener) {
            binding.ivSocialPostItemThumbnail.loadImage(post.imageList[0])
            binding.tvSocialPostItemTitle.text = post.title
            binding.ivSocialPostItemProfilePicture.loadImage(post.authorProfilePicture)
            binding.tvSocialPostItemNickname.text = post.nickname
            binding.tvSocialPostItemSavedCount.text = post.savedCount.toString()

            // 랭킹 뱃지 처리
            if (post.rank != null) {
                binding.tvRankBadge.visibility = View.VISIBLE
                binding.tvRankBadge.text = post.rank.toString()
            } else {
                binding.tvRankBadge.visibility = View.GONE
            }

            // 클릭 이벤트 설정
            binding.root.setOnClickListener {
                listener.onPostItemClick(post)
            }
        }
    }
}

interface OnPostItemClickListener {
    fun onPostItemClick(post: Post)
}