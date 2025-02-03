package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseState
import com.nemodream.bangkkujaengi.customer.data.repository.OrderHistoryRepository
import com.nemodream.bangkkujaengi.customer.data.repository.ShoppingCartRepository
import com.nemodream.bangkkujaengi.customer.deliverytracking.RetrofitInstance
import com.nemodream.bangkkujaengi.customer.deliverytracking.TrackingDetail
import com.nemodream.bangkkujaengi.customer.ui.adapter.OrderDetailsAdapter.OrderDetailsViewHolder
import com.nemodream.bangkkujaengi.customer.ui.adapter.PaymentProductAdapter.PaymentProductViewHolder
import com.nemodream.bangkkujaengi.customer.ui.adapter.ShippingStatusAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ShippingStatusViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentShippingStatusBinding
import com.nemodream.bangkkujaengi.utils.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ShippingStatusFragment : Fragment() {

    lateinit var fragmentShippingStatusBinding: FragmentShippingStatusBinding
    val shippingStatusViewModel: ShippingStatusViewModel by viewModels()

    var document_id = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentShippingStatusBinding = FragmentShippingStatusBinding.inflate(inflater, container, false)

        // 툴바 세팅
        setting_toolbar()

        // document_id 세팅
        settiing_document_id()

        // 배송 조회 한 상품 데이터 저장
        setting_shipping_purchase_product()

        // 텍스트 세팅
        setting_text()
        // 이미지 세팅
        setting_image()
        // 배송 상태에 따른 배송 단계 이미지 변경 메소드
        setting_shipping_status_image()
        // 배송 상태 상세 내역 리사이클러뷰 세팅
        setting_shipping_status_recyclerview()
        
        // 배송 상세 내용 저장 저장
        // setting_shipping_status_details()

        return fragmentShippingStatusBinding.root
    }


    // 배송 상세 내용 관련 ////////////////////////////////////////////////////////////////////////////

    // 배송 상태 상세 내역 리사이클러뷰 세팅
    fun setting_shipping_status_recyclerview() {
        // 배송 상태 상세 내역 옵저버
        shippingStatusViewModel.shipping_status_list.observe(viewLifecycleOwner) {
            fragmentShippingStatusBinding.rvShippingStatusDetails.adapter = ShippingStatusAdapter(
                shippingStatusViewModel
            )
            fragmentShippingStatusBinding.rvShippingStatusDetails.layoutManager = LinearLayoutManager(requireContext())   
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // 배송 조회 한 상품 데이터 저장
    fun setting_shipping_purchase_product() {
        viewLifecycleOwner.lifecycleScope.launch {
            val work1 = async(Dispatchers.IO) {
                OrderHistoryRepository.getting_purchase_data_by_document_id(document_id)
            }.await()
            shippingStatusViewModel.shipping_purchase_product.value = work1

            Log.d("shipping_purchase_product", "${shippingStatusViewModel.shipping_purchase_product.value}")

        }

    }

    // document_id 세팅
    fun settiing_document_id() {
        arguments?.let {
            // 전달받은 구매내역 documentId를 변수에 저장
            document_id = ShippingStatusFragmentArgs.fromBundle(it).productDocumentId
            val payment_date_time = ShippingStatusFragmentArgs.fromBundle(it).paymentDate
            // 받은 데이터를 MutableLiveData에 저장
            val receivedTrackingResponse = ShippingStatusFragmentArgs.fromBundle(it).trackingResponse

            // 입력 형식 정의
            val inputFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            // 출력 형식 정의
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            // 입력 날짜 파싱 후 새로운 형식으로 변환
            val date = inputFormat.parse(payment_date_time)

            // 기존 trackingDetails에 임의 데이터 추가 및 기존 데이터 밀기
            val customDetail = TrackingDetail(
                timeString = outputFormat.format(date!!), // 예시 데이터
                where = "결제 완료",
                kind = "곧 배송이 시작됩니다"
            )

            val updatedTrackingDetails = mutableListOf(customDetail) // 새로운 리스트에 임의 데이터 추가
            updatedTrackingDetails.addAll(receivedTrackingResponse!!.trackingDetails) // 기존 데이터를 추가

            Log.d("trackingDetails1", "${updatedTrackingDetails}")

            // 새로운 TrackingResponse 객체 생성
            val updatedTrackingResponse = receivedTrackingResponse!!.copy(
                trackingDetails = updatedTrackingDetails
            )

            Log.d("trackingDetails2", "${updatedTrackingResponse}")

            // ViewModel의 LiveData 업데이트
            shippingStatusViewModel.shipping_status_list.value = updatedTrackingResponse
        }

        Log.d("document_id", "$document_id")
        Log.d("shipping_status_list2", "${shippingStatusViewModel.shipping_status_list.value}")
    }
    
    // 텍스트 세팅
    fun setting_text() {

        shippingStatusViewModel.shipping_purchase_product.observe(viewLifecycleOwner) {

            // 받는 사람
            fragmentShippingStatusBinding.tvShippingStatusRecipientName.text = it.receiverName
            // 받는 주소
            fragmentShippingStatusBinding.tvShippingStatusRecipientAddress.text = "${it.receiverAddr}\n${it.receiverDetailAddr}"

            // 상품명
            fragmentShippingStatusBinding.tvShippingStatusOrderProductName.text = it.productTitle
            // 옵션
            if (it.color == "") {
                fragmentShippingStatusBinding.tvShippingStatusOrderProductOption.text =
                    "옵션 : 없음"
            }
            else {
                fragmentShippingStatusBinding.tvShippingStatusOrderProductOption.text =
                    "옵션 : " + it.color
            }
            // 주문 갯수
            fragmentShippingStatusBinding.tvShippingStatusOrderProductCnt.text = "${it.purchaseQuantity} 개"
            // 가격
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it.totPrice) + " 원"
            fragmentShippingStatusBinding.tvShippingStatusOrderProductPrice.text = formattedPrice

        }

    }

    // 이미지 로드 메소드
    fun setting_image() {

        shippingStatusViewModel.shipping_purchase_product.observe(viewLifecycleOwner) {

            Log.d("shipping_purchase_product_images", "${shippingStatusViewModel.shipping_purchase_product.value!!.images}")
            // 상품 이미지 로드
            viewLifecycleOwner.lifecycleScope.launch {
                val work1 = async(Dispatchers.IO) {
                    ShoppingCartRepository.getting_image(shippingStatusViewModel.shipping_purchase_product.value!!.images)
                }
                val uri_image = work1.await()

                fragmentShippingStatusBinding.ivShippingStatusOrderProductImage.loadImage(uri_image)
            }
        }
    }

    // 배송 상태에 따른 배송 단계 이미지 변경 메소드
    fun setting_shipping_status_image() {
        shippingStatusViewModel.shipping_status_list.observe(viewLifecycleOwner) {

            // 송장 번호 텍스트
            fragmentShippingStatusBinding.tvShippingStatusInvoiceNumber.text = it.invoiceNo

            when (it.level) {
                // 상품 준비중 단계일 경우
                1 -> {
                    // 상품 준비중 단계 이미지, 텍스트 변경
                    fragmentShippingStatusBinding.ivShippingStatusReadyStatus.setImageResource(R.drawable.package_2_black_72px)
                    fragmentShippingStatusBinding.tvShippingStatusReadyStatus.setTextColor(Color.parseColor("#000000"))
                    fragmentShippingStatusBinding.ivShippingStatusReadyStatusCheck.setImageResource(R.drawable.check_circle_24px)
                }
                // 배송중 단계일 경우
                2,3,4,5 -> {
                    // 상품 준비중 단계 이미지, 텍스트 변경
                    fragmentShippingStatusBinding.ivShippingStatusReadyStatus.setImageResource(R.drawable.package_2_black_72px)
                    fragmentShippingStatusBinding.tvShippingStatusReadyStatus.setTextColor(Color.parseColor("#000000"))
                    fragmentShippingStatusBinding.ivShippingStatusReadyStatusCheck.setImageResource(R.drawable.check_circle_24px)
                    // 배송중 단계 이미지, 텍스트 변경
                    fragmentShippingStatusBinding.ivShippingStatusDeliveryStatus.setImageResource(R.drawable.delivery_truck_speed_black_72px)
                    fragmentShippingStatusBinding.tvShippingStatusDeliveryStatus.setTextColor(Color.parseColor("#000000"))
                    fragmentShippingStatusBinding.ivShippingStatusDeliveryStatusCheck.setImageResource(R.drawable.check_circle_24px)
                }

                6 -> {
                    // 상품 준비중 단계 이미지, 텍스트 변경
                    fragmentShippingStatusBinding.ivShippingStatusReadyStatus.setImageResource(R.drawable.package_2_black_72px)
                    fragmentShippingStatusBinding.tvShippingStatusReadyStatus.setTextColor(Color.parseColor("#000000"))
                    fragmentShippingStatusBinding.ivShippingStatusReadyStatusCheck.setImageResource(R.drawable.check_circle_24px)
                    // 배송중 단계 이미지, 텍스트 변경
                    fragmentShippingStatusBinding.ivShippingStatusDeliveryStatus.setImageResource(R.drawable.delivery_truck_speed_black_72px)
                    fragmentShippingStatusBinding.tvShippingStatusDeliveryStatus.setTextColor(Color.parseColor("#000000"))
                    fragmentShippingStatusBinding.ivShippingStatusDeliveryStatusCheck.setImageResource(R.drawable.check_circle_24px)
                    // 배송완료 단계 이미지, 텍스트 변경
                    fragmentShippingStatusBinding.ivShippingStatusCompleteStatus.setImageResource(R.drawable.select_check_box_black_72px)
                    fragmentShippingStatusBinding.tvShippingStatusCompleteStatus.setTextColor(Color.parseColor("#000000"))
                    fragmentShippingStatusBinding.ivShippingStatusCompleteStatusCheck.setImageResource(R.drawable.check_circle_24px)
                }
                // 결제 완료 단계일 경우
                else -> {
                    fragmentShippingStatusBinding.ivShippingStatusPaymentStatus.setImageResource(R.drawable.credit_card_black_72px)
                    fragmentShippingStatusBinding.tvShippingStatusPaymentStatus.setTextColor(Color.parseColor("#000000"))
                    fragmentShippingStatusBinding.ivShippingStatusPaymentStatusCheck.setImageResource(R.drawable.check_circle_24px)
                }
            }

        }
    }

    // 툴바 세팅
    fun setting_toolbar() {
        fragmentShippingStatusBinding.tbShippingStatus.apply {
            // 툴바에 뒤로가기 버튼 아이콘 생성
            setNavigationIcon(R.drawable.ic_arrow_back)
            // 툴바 뒤로가기 버튼의 이벤트
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    // 배송 상세 내용 저장
//    fun setting_shipping_status_details() {
//        shippingStatusViewModel.shipping_purchase_product.observe(viewLifecycleOwner) {
//            fetchTrackingInfo("04", it.purchaseInvoiceNumber.toString())
//        }
//    }

    // api를 통해 배송 상세 내용을 저장
//    fun fetchTrackingInfo(courierCode: String, invoiceNumber: String) {
//        viewLifecycleOwner.lifecycleScope.launch {
//            try {
//                val response = withContext(Dispatchers.IO) {
//                    RetrofitInstance.api.getTrackingInfo(
//                        apiKey = "qo9jf3bIFZ4aEzIscfGPPQ",
//                        courierCode = courierCode,
//                        invoiceNumber = invoiceNumber
//                    )
//                }
//
//                shippingStatusViewModel.shipping_status_list.value = response
//
//                shippingStatusViewModel.shipping_status_list.value!!.trackingDetails.forEach {
//                    Log.d("trackingDetails", "${it.timeString} ${it.where} ${it.kind}")
//                }
//
//            } catch (e: HttpException) {
//                Log.d("HTTP 오류 발생", "HTTP 오류 발생: ${e.code()} - ${e.message()}")
//            } catch (e: Exception) {
//                Log.d("기타 오류 발생", "기타 오류 발생: ${e.message}")
//            }
//        }
//    }


}