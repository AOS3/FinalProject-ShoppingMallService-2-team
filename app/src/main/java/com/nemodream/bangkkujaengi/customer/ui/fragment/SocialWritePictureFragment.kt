package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialCarouselAdapter
import com.nemodream.bangkkujaengi.databinding.FragmentSocialWritePictureBinding

class SocialWritePictureFragment : Fragment() {

    private var _binding: FragmentSocialWritePictureBinding? = null
    private val binding get() = _binding!!

    private val selectedPhotos = mutableListOf<Uri>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSocialWritePictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupCarousel()

        // BottomSheet에서 전달된 선택된 사진 데이터를 수신
        childFragmentManager.setFragmentResultListener("selectedPhotos", this) { _, bundle ->
            val photos = bundle.getParcelableArrayList<Uri>("photos")
            if (!photos.isNullOrEmpty()) {
                updateSelectedPhotos(photos)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        with(binding) {
            toolbarSocial.setNavigationOnClickListener {
                findNavController().popBackStack(R.id.navigation_social, false)
            }
            btnAddPicture.setOnClickListener {
                val bottomSheetFragment = SocialWritePictureBottomSheetFragment()
                bottomSheetFragment.show(childFragmentManager, "SocialWritePictureBottomSheetFragment")
            }
        }
    }

    // 선택된 사진 리스트를 업데이트
    private fun updateSelectedPhotos(photos: List<Uri>) {
        selectedPhotos.clear() // 이거 없애면 캐러셀에 사진 추가 할 수 있음
        selectedPhotos.addAll(photos)

        // Placeholder 숨기기 및 Carousel 표시
        binding.viewSocialWritePicturePlaceholder.visibility = View.GONE
        binding.tvSocialWritePicturePlaceholder.visibility = View.GONE
        binding.rvSocialWritePictureSelectedPhotos.visibility = View.VISIBLE

        binding.rvSocialWritePictureSelectedPhotos.adapter?.notifyDataSetChanged()
    }

    private fun setupCarousel() {
        binding.rvSocialWritePictureSelectedPhotos.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = SocialCarouselAdapter(selectedPhotos)
        }
    }
}
