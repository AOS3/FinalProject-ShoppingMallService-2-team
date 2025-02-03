package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nemodream.bangkkujaengi.databinding.ItemSocialCarouselPhotoBinding

class SocialCarouselAdapter(
    private val photos: List<Uri>,
    private val onPhotoClick: (Int, Float, Float) -> Unit // 클릭 이벤트 콜백 추가
) : RecyclerView.Adapter<SocialCarouselAdapter.CarouselViewHolder>() {

    inner class CarouselViewHolder(private val binding: ItemSocialCarouselPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photoUri: Uri) {
            // 이미지 로드
            Glide.with(binding.root.context)
                .load(photoUri)
                .into(binding.ivCarouselPhoto)

            // 터치 이벤트에서는 위치만 전달
            binding.ivCarouselPhoto.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    // 클릭된 위치 정보만 콜백으로 전달
                    onPhotoClick(adapterPosition, event.x, event.y)
                }
                true
            }
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
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size
}
