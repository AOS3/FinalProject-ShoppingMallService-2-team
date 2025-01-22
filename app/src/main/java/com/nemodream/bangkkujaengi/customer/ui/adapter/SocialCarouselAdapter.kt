package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nemodream.bangkkujaengi.databinding.ItemSocialCarouselPhotoBinding

class SocialCarouselAdapter(private val selectedPhotos: List<Uri>) :
    RecyclerView.Adapter<SocialCarouselAdapter.CarouselViewHolder>() {

    inner class CarouselViewHolder(private val binding: ItemSocialCarouselPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photoUri: Uri) {
            Glide.with(binding.root.context)
                .load(photoUri)
                .into(binding.ivCarouselPhoto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val binding = ItemSocialCarouselPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CarouselViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(selectedPhotos[position])
    }

    override fun getItemCount(): Int = selectedPhotos.size
}
