package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.databinding.ItemProfileGalleryBinding
import com.nemodream.bangkkujaengi.utils.loadImage

class GalleryAdapter(
    private val onImageClickListener: OnImageClickListener
) : PagingDataAdapter<Uri, GalleryAdapter.GalleryViewHolder>(GalleryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(
            ItemProfileGalleryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onImageClickListener = { position ->
                getItem(position)?.let { uri -> onImageClickListener.onImageClick(uri) }
            }
        )
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        getItem(position)?.let { uri ->
            holder.bind(uri)
        }
    }

    class GalleryViewHolder(
        private val binding: ItemProfileGalleryBinding,
        private val onImageClickListener: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onImageClickListener(bindingAdapterPosition)
            }
        }

        fun bind(uri: Uri) {
            binding.ivGalleryItem.loadImage("$uri")
        }
    }

    object GalleryDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
}

interface OnImageClickListener {
    fun onImageClick(uri: Uri)
}