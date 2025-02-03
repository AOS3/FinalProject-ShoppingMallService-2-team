package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.Tag
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialCarouselAdapter
import com.nemodream.bangkkujaengi.databinding.FragmentSocialWritePictureBinding
import com.nemodream.bangkkujaengi.databinding.ItemSocialTagProductLabelBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nemodream.bangkkujaengi.customer.data.model.Post
import com.nemodream.bangkkujaengi.utils.getUserId


class SocialWritePictureFragment : Fragment() {

    private var _binding: FragmentSocialWritePictureBinding? = null
    private val binding get() = _binding!!

    private val selectedPhotos = mutableListOf<Uri>() // 게시할 사진들 리스트
    private val productTagPinList = mutableListOf<Tag>() // 태그 리스트

    
    private lateinit var appContext: Context

    // Firebase Firestore와 FirebaseAuth 초기화
    private val firestore = FirebaseFirestore.getInstance()


    private val carouselAdapter: SocialCarouselAdapter by lazy {
        SocialCarouselAdapter(selectedPhotos) { position, x, y ->
            openWriteTagBottomSheet(position, x, y)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSocialWritePictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupViewPager()

        // PictureBottomSheet에서 전달된 선택된 사진 데이터를 수신
        childFragmentManager.setFragmentResultListener("selectedPhotos", this) { _, bundle ->
            val photos = bundle.getParcelableArrayList<Uri>("photos")
            if (!photos.isNullOrEmpty()) {
                updateSelectedPhotos(photos)
            }
        }

        // TagBottomSheet에서 전달된 상품 데이터 수신
        childFragmentManager.setFragmentResultListener("productWithTagData", this) { _, bundle ->
            val product = bundle.getParcelable<Product>("selectedProduct")
            val position = bundle.getInt("photoPosition", -1)
            val xCoord = bundle.getFloat("xCoord", 0f)
            val yCoord = bundle.getFloat("yCoord", 0f)

            // 전달받은 데이터를 로그로 확인 (필요에 따라 추가 처리)
            Log.d("SocialWritePictureFragment", "Product: $product, Position: $position, X: $xCoord, Y: $yCoord")

            if (product != null && position != -1) {
                addTagPin(position, xCoord, yCoord, product)

                // Tag 데이터 생성 및 리스트에 추가
                val tag = Tag(order = position, tagX = xCoord, tagY = yCoord, tagProductInfo = product)
                productTagPinList.add(tag)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 태그 핀 추가 함수
    private fun addTagPin(position: Int, x: Float, y: Float, product: Product) {
        val container = (binding.vpSocialWritePictureCarousel[0] as RecyclerView)
            .findViewHolderForAdapterPosition(position)?.itemView as? FrameLayout

        val tagPin = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_tag_pin)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = (x - 20).toInt()
                topMargin = (y - 20).toInt()
            }
        }

        container?.addView(tagPin)

        val labelBinding = ItemSocialTagProductLabelBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        labelBinding.tvTagProductLabelName.text = getShortenedProductName(product.productName)
        labelBinding.tvTagProductLabelPrice.text = "${product.price}원"

        // 상품 이미지 설정 (예시로 Glide 사용)
        Glide.with(requireContext())
            .load(product.images[0]) // 상품 이미지 URL
            .into(labelBinding.ivTagProductLabelImage)

        // 라벨 위치 설정
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            leftMargin = (x + 40).toInt() // 태그 오른쪽에 라벨 배치
            topMargin = (y - 10).toInt()  // 태그 위에 라벨 배치
        }

        // 라벨을 처음에는 숨김
        labelBinding.root.visibility = View.GONE

        container?.addView(labelBinding.root, params)

        // 태그와 라벨 클릭 리스너 설정
        setupTagAndLabelListeners(tagPin, labelBinding, container, position, x, y)
    }



    // Firestore에서 memberNickName 필드를 가져오는 함수
    fun getMemberNickName(userId: String, callback: (String?) -> Unit) {
        firestore.collection("Member")
            .document(userId)
            .get()
            .addOnSuccessListener { callback(it.getString("memberNickName")) }
    }

    // Firestore에서 memberProfileImage 필드를 가져오는 함수
    fun getMemberProfileImage(userId: String, callback: (String?) -> Unit) {
        firestore.collection("Member")
            .document(userId)
            .get()
            .addOnSuccessListener { callback(it.getString("memberProfileImage")) }
    }

    // 이미지를 로컬저장소에서 Firebase Storage에 업로드
    // 업로드가 완료되면 해당 이미지의 다운로드 URL을 반환
    fun uploadImageToFirebaseStorage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // 업로드 성공 후, 다운로드 URL 가져오기
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())  // 이미지 URL 반환
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
            callback(null)  // 실패 시 null 반환
        }
    }

    // 업로드된 이미지의 URL을 Firestore에 저장
    // Firestore에 게시글을 저장할 때, 각 이미지를 Firebase Storage에 업로드하고 그 URL을 imageList에 저장
    fun uploadImagesAndCreatePost(selectedPhotos: List<Uri>, callback: (List<String>) -> Unit) {
        val imageUrls = mutableListOf<String>()
        var uploadCount = 0

        selectedPhotos.forEach { imageUri ->
            uploadImageToFirebaseStorage(appContext, imageUri) { imageUrl ->
                if (imageUrl != null) {
                    imageUrls.add(imageUrl)
                }
                uploadCount++

                // 모든 이미지가 업로드된 후에 콜백 호출
                if (uploadCount == selectedPhotos.size) {
                    callback(imageUrls)
                }
            }
        }
    }

    // 리스너 모음
    private fun setupListeners() {
        with(binding) {
            // 툴바의 뒤로가기 아이콘
            toolbarSocial.setNavigationOnClickListener {
                findNavController().popBackStack(R.id.navigation_social, false)
            }

            // "사진 추가" 버튼
            btnAddPicture.setOnClickListener {
                openWritePictureBottomSheet()
            }

            // "항목 수정" 버튼
            btnModifyItem.setOnClickListener {
                openWritePictureBottomSheet()
            }

            // 사진 추가 화면에서 "다음" 버튼
            btnWritePictureNext.setOnClickListener {
                binding.btnWritePictureNext.visibility = View.GONE
                binding.btnModifyItem.visibility = View.GONE
                binding.tvTagGuidance.visibility = View.GONE
                binding.tfWriteTitle.visibility = View.VISIBLE
                binding.tfWriteContent.visibility = View.VISIBLE
                binding.btnPost.visibility = View.VISIBLE
            }

            binding.btnPost.setOnClickListener {
                val title = binding.tfWriteTitle.text.toString()
                val content = binding.tfWriteContent.text.toString()

                if (title.isEmpty() || content.isEmpty() || selectedPhotos.isEmpty()) {
                    Toast.makeText(context, "제목, 내용, 사진을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 이미지 업로드 후 Firestore에 게시글 저장
                uploadImagesAndCreatePost(selectedPhotos) { imageUrls ->
                    // Firestore에서 memberNickName 가져오기
                    getMemberNickName(appContext.getUserId()) { memberNickName ->
                        getMemberProfileImage(appContext.getUserId()) { memberProfileImage ->
                            // Post 객체 생성
                            val post = Post(
                                id = firestore.collection("posts").document().id, // 자동 생성된 ID
                                memberDocId = appContext.getUserId(),
                                nickname = memberNickName ?: "익명", // 닉네임 없으면 "익명" 처리
                                authorProfilePicture = memberProfileImage
                                    ?: "", // 프로필 이미지 없으면 빈 문자열 처리
                                title = title,
                                content = content,
                                imageList = imageUrls, // 이미지 Uri를 문자열로 변환
                                productTagPinList = productTagPinList,
                                savedCount = 0, // 초기값
                                commentCount = 0 // 초기값
                            )

                            // Firestore에 게시글 저장
                            firestore.collection("Post")
                                .document(post.id)
                                .set(post)
                                .addOnSuccessListener {
                                    findNavController().popBackStack() // 게시글 작성 후 이전 화면으로 돌아가기
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "게시글 작성에 실패했습니다: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
            }
        }
    }

    // 태그와 라벨 클릭 리스너
    private fun setupTagAndLabelListeners(
        tagPin: ImageView,
        labelBinding: ItemSocialTagProductLabelBinding,
        container: FrameLayout?,
        position: Int,
        x: Float,
        y: Float
    ) {
        // 태그 클릭 시 라벨 보이기/숨기기
        tagPin.setOnClickListener {
            if (labelBinding.root.visibility == View.GONE) {
                labelBinding.root.visibility = View.VISIBLE // 라벨 보이기
            } else {
                labelBinding.root.visibility = View.GONE // 라벨 숨기기
            }
        }

        // 삭제 버튼 클릭 시 태그와 라벨 제거
        labelBinding.tvTagProductLabelDelete.setOnClickListener {
            // 태그 삭제
            container?.removeView(tagPin)
            // 라벨 삭제
            container?.removeView(labelBinding.root)
            // 태그 리스트에서 해당 태그 삭제
            productTagPinList.removeAll { it.order == position && it.tagX == x && it.tagY == y }
        }
    }

    // 상품명 글자수 제한 함수
    private fun getShortenedProductName(productName: String): String {
        return if (productName.length > 7) {
            "${productName.substring(0, 7)}..."
        } else {
            productName
        }
    }

    private fun setupViewPager() {
        binding.vpSocialWritePictureCarousel.adapter = carouselAdapter
    }

    // 선택된 사진 리스트를 업데이트
    private fun updateSelectedPhotos(photos: List<Uri>) {
        selectedPhotos.clear()
        selectedPhotos.addAll(photos)

        // Placeholder 숨기기 및 Carousel 표시
        binding.viewSocialWritePicturePlaceholder.visibility = View.GONE
        binding.tvSocialWritePicturePlaceholder.visibility = View.GONE
        binding.flSocialWritePictureContainer.visibility = View.VISIBLE

        // 버튼 상태 업데이트
        binding.btnAddPicture.visibility = View.GONE
        binding.btnModifyItem.visibility = View.VISIBLE
        binding.btnWritePictureNext.visibility = View.VISIBLE
        binding.tvTagGuidance.visibility = View.VISIBLE

        binding.vpSocialWritePictureCarousel.adapter?.notifyDataSetChanged()
    }

    // 사진 선택 바텀시트 올리기
    private fun openWritePictureBottomSheet() {
        val bottomSheetFragment = SocialWritePictureBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("selectedPhotos", ArrayList(selectedPhotos))
            }
        }
        bottomSheetFragment.show(childFragmentManager, "SocialWritePictureBottomSheetFragment")
    }

    // 태그 추가 바텀시트 올리기
    private fun openWriteTagBottomSheet(position: Int, x: Float, y: Float) {
        val bottomSheetFragment = SocialWriteTagBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putInt("photoPosition", position)
                putFloat("xCoord", x)
                putFloat("yCoord", y)
            }
        }
        bottomSheetFragment.show(childFragmentManager, "SocialWriteTagBottomSheetFragment")
    }
}