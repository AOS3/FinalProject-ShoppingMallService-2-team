package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.webkit.WebViewAssetLoader
import com.google.firebase.Timestamp
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import com.nemodream.bangkkujaengi.customer.data.model.CouponType
import com.nemodream.bangkkujaengi.customer.data.model.PaymentProduct
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseState
import com.nemodream.bangkkujaengi.customer.data.repository.PaymentRepository
import com.nemodream.bangkkujaengi.customer.ui.adapter.PaymentProductAdapter
import com.nemodream.bangkkujaengi.customer.ui.adapter.SelectCouponAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.PaymentViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentPaymentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentFragment : Fragment() {

    lateinit var fragmentPaymentBinding: FragmentPaymentBinding

    val paymentViewModel: PaymentViewModel by viewModels()

    // 테스트
    val testData = Array(3) {
        "상품명 $it"
    }

    // 유저 ID
    var user_id: String = ""
    // 회원 or 비회원
    var user_type: String = ""
    // 유저 이름
    var user_name: String = ""
    // 유저 전화번호
    var user_phone_number: String = ""
    // 유저 주소
    var user_address: String = ""

    var order_type = ""

    var payment_product_user_id = ""

    // 장바구니에서 체크표시한 상품의 갯수, 상품 문서 id 등을 담는 변수
    var payment_product_list = PaymentProduct()

    // 결제할 상품 정보를 담을 리스트
    // var payment_product_data_list = mutableListOf<Product>()

    // 선택된 쿠폰 목록을 담을 리스트
    var checked_coupon_document_id_list = mutableListOf<String>()

    // 구매 내역 리스트
    var purchase_product = mutableListOf<Purchase>()


    // var select_coupon_list = mutableListOf<Coupon>()

    var selectedPosition = 0

    var selectedDocumentId = ""



    private lateinit var webView: WebView
    private lateinit var dialog: Dialog
    private lateinit var assetLoader: WebViewAssetLoader


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentPaymentBinding = FragmentPaymentBinding.inflate(inflater, container, false)

        // 툴바 세팅
        setting_toolbar()
        // 유저 아이디 세팅
        getting_user_id()
        // 주문 목록 리사이클러뷰 설정
        // setting_recycledrview_order()
        // 버튼 설정
        setting_button()
        // textInputLayout의 값을 세팅하고 업데이트 하는 메소드 호출
        setting_textInputLayout_delivery_address()
        // 결제 금액 텍스트 옵저버 세팅
        setting_payment_amount_text_observe()
        // 선택된 쿠폰 옵저버 세팅
        // setting_select_coupon_observe()

        
        // 선택된 쿠폰 목록 recyclerview 설정
        setting_recyclerview_select_coupon()

        // 선택된 쿠폰 리스트 초기화
        // PaymentCouponBottomSheetFragment에서 결과 수신
        setting_checked_coupon_list()

        // 결제 하기 버튼 클릭 관련
        setting_btn_payment_make_payment()

        // 주소 찾기 버튼 클릭 관련
        setting_btn_payment_zip_code()

        return fragmentPaymentBinding.root
    }

    // 툴바 세팅
    fun setting_toolbar() {
        fragmentPaymentBinding.tbPayment.apply {
            // 툴바에 뒤로가기 버튼 아이콘 생성
            setNavigationIcon(R.drawable.ic_arrow_back)
            // 툴바 뒤로가기 버튼의 이벤트
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }


    // 주소 찾기 버튼 클릭 관련 ///////////////////////////////////////////////////////////////////////
    // **주소 찾기 버튼 클릭 이벤트**
    private fun setting_btn_payment_zip_code() {
        fragmentPaymentBinding.btnPaymentZipCode.setOnClickListener {
            showWebViewDialog()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWebViewDialog() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }

        // 다이얼로그 생성
        dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_webview)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        val btnCloseWebView: ImageView = dialog.findViewById(R.id.btnCloseWebView)
        val webViewContainer: FrameLayout = dialog.findViewById(R.id.webViewContainer)

        // WebView 동적 생성 (기존 WebView를 제거 후 추가)
        webView = WebView(requireContext()).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
        }

        // WebViewAssetLoader 설정
        assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(requireContext()))
            .build()

        // WebViewClient 설정
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }

        // WebChromeClient 설정
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?
            ): Boolean {
                val newWebView = WebView(requireContext()).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                }
                webViewContainer.addView(newWebView) // 기존 UI 유지하면서 WebView 추가

                newWebView.webChromeClient = object : WebChromeClient() {
                    override fun onCloseWindow(window: WebView?) {
                        webViewContainer.removeView(newWebView) // 창 닫기 시 제거
                    }
                }

                (resultMsg?.obj as? WebView.WebViewTransport)?.webView = newWebView
                resultMsg?.sendToTarget()
                return true
            }
        }

        // JavaScript 인터페이스 추가
        webView.addJavascriptInterface(WebAppInterface(), "Android")

        // WebView를 컨테이너에 추가
        webViewContainer.addView(webView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // 웹페이지 로드
        webView.loadUrl("https://appassets.androidplatform.net/assets/postcode.html")

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        btnCloseWebView.setOnClickListener {
            dialog.dismiss()
        }

        // 다이얼로그 표시
        dialog.show()
    }


    // JavaScript 인터페이스 정의
    inner class WebAppInterface {
        @JavascriptInterface
        fun processDATA(postcode: String, address: String, extraAddress: String) {
            requireActivity().runOnUiThread {
                fragmentPaymentBinding.tilPaymentAddress.editText?.setText(address)
                fragmentPaymentBinding.tilPaymentZipCode.editText?.setText(postcode)
                dialog.dismiss()
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    // 결제 하기 버튼 클릭 관련 ///////////////////////////////////////////////////////////////////////
    // 결제 버튼 클릭 메소드
    fun setting_btn_payment_make_payment() {

        fragmentPaymentBinding.btnPaymentMakePayment.setOnClickListener {

            // 구매하기 버튼 클릭 시 필드 값 검증
            val name = fragmentPaymentBinding.tilPaymentName.editText?.text.toString().trim()
            val phone = fragmentPaymentBinding.tilPaymentPhoneNumber.editText?.text.toString().trim()
            val zip = fragmentPaymentBinding.tilPaymentZipCode.editText?.text.toString().trim()
            val address = fragmentPaymentBinding.tilPaymentAddress.editText?.text.toString().trim()
            val detailedAddress = fragmentPaymentBinding.tilPaymentDetailedAddress.editText?.text.toString().trim()
            // 회원이 아닐 경우에만 비회원 배송 조회 비밀번호를 검증합니다.
            val nonMemberPassword = fragmentPaymentBinding.tilPaymentNonMemberPassword.editText?.text.toString().trim()

            // user_type이 "member"가 아니라면 비회원 비밀번호도 필수
            if (name.isEmpty() || phone.isEmpty() || zip.isEmpty() || address.isEmpty() || detailedAddress.isEmpty() ||
                (user_type != "member" && nonMemberPassword.isEmpty())) {
                // 하나라도 입력되지 않았다면 오류 메시지 출력 후 진행 중단
                showCustomToast("모든 필드를 입력해 주세요.", R.drawable.ic_symbol)
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                // 장바구니 항목 중 구매 항목 삭제
                val work1 = async(Dispatchers.IO) {
                    Log.d("1122", "${payment_product_list.items.map { it.productId }}")
                    PaymentRepository.remove_cart_item_by_payment_product_document_id_list(user_id, payment_product_list.items.map { it.productId })
                }.await()

                val time_stamp = Timestamp.now()
                var position = 0

                val work2 = async(Dispatchers.IO) {
                    payment_product_list.items.map {
                        // 할인 후 가격 //////////////////////////////////////////////////////////////
                        // 원가
                        val originalPrice = paymentViewModel.payment_product_data_list.value!![position].price
                        // 할인율
                        val discountRate = paymentViewModel.payment_product_data_list.value!![position].saleRate
                        // 할인 후 가격
                        val tot_price = when(user_type) {
                            "member" -> {
                                ((originalPrice * (1 - (discountRate / 100.0))).toInt() * payment_product_list.items[position].quantity)
                            }
                            else -> {
                                paymentViewModel.payment_product_data_list.value!![position].price * payment_product_list.items[position].quantity
                            }
                        }
                        ////////////////////////////////////////////////////////////////////////////

                        // Timestamp를 Date로 변환 후 포맷 적용
                        val timeStampFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
                        val formattedTime = timeStampFormat.format(time_stamp.toDate())

                        val purchase = Purchase(
                            memberId = user_id,
                            purchaseId = user_id + formattedTime,
                            nonMemberPassword = fragmentPaymentBinding.tilPaymentNonMemberPassword.editText?.text.toString(),
                            productTitle = paymentViewModel.payment_product_data_list.value!![position].productName,
                            color = it.color,
                            images = paymentViewModel.payment_product_data_list.value!![position].images[0],
                            productId = it.productId,
                            productCost = paymentViewModel.payment_product_data_list.value!![position].price * it.quantity,
                            couponSalePrice = paymentViewModel.tv_payment_coupon_sale_price_text.value!!,
                            saleRate = paymentViewModel.payment_product_data_list.value!![position].saleRate,
                            totPrice = tot_price,
                            purchaseDate = time_stamp,
                            purchaseState = PurchaseState.PAYMENT_COMPLETED.name,
                            purchaseInvoiceNumber = 0,
                            purchaseQuantity = it.quantity,
                            Delete = false,
                            purchaseDateTime = formattedTime,
                            deliveryCost = paymentViewModel.tv_payment_tot_delivery_cost_text.value!!,
                            receiverName = fragmentPaymentBinding.tilPaymentName.editText?.text.toString(),
                            receiverZipCode = fragmentPaymentBinding.tilPaymentZipCode.editText?.text.toString(),
                            receiverAddr = fragmentPaymentBinding.tilPaymentAddress.editText?.text.toString(),
                            receiverDetailAddr = fragmentPaymentBinding.tilPaymentDetailedAddress.editText?.text.toString(),
                            receiverPhone = fragmentPaymentBinding.tilPaymentPhoneNumber.editText?.text.toString()
                        )

                        purchase_product.add(purchase)

                        position++
                    }

                    // 구매 항목 데이터 저장
                    PaymentRepository.add_purchase_product(purchase_product)

                }.await()

                val work3 = async(Dispatchers.IO) {
                    payment_product_list.items.map {
                        PaymentRepository.update_product_count_purchase_count(it.productId, it.quantity)
                    }
                }.await()

                purchase_product.forEach {
                    Log.d("2233", "purchase_product : ${it}")
                }

                val action = PaymentFragmentDirections.actionPaymentFragmentToPaymentCompletedFragment()
                findNavController().navigate(action)

            }
        }

    }

    // 토스트 메세지 띄우는 메소드
    private fun showCustomToast(message: String, iconResId: Int) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custom_toast_layout, null)

        val toastIcon: ImageView = layout.findViewById(R.id.toast_icon)
        val toastText: TextView = layout.findViewById(R.id.toast_text)

        toastIcon.setImageResource(iconResId) // 아이콘 변경
        toastText.text = message // 메시지 변경

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 200)
        toast.show()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    // PaymentCouponBottomSheetFragment에서 결과 수신
    fun setting_checked_coupon_list() {

        parentFragmentManager.setFragmentResultListener("couponResultKey", viewLifecycleOwner) { requestKey, result ->
            // 선택한 쿠폰 리스트 초기화
            paymentViewModel.select_coupon_list.value?.clear()
            // 선택한 쿠폰 문서 id 리스트 초기화
            checked_coupon_document_id_list.clear()

            this.selectedPosition = result.getInt("select_position")
            this.selectedDocumentId = result.getString("select_document_id")!!

            Log.d("123456", "selectedPosition : ${selectedPosition}")
            Log.d("123456", "selectedDocumentId : ${selectedDocumentId}")

            checked_coupon_document_id_list.clear()

            // 쿠폰 리스트 중 선택된 position 을 ViewModel 에 설정
            paymentViewModel.checked_position.value = selectedPosition
            checked_coupon_document_id_list.add(selectedDocumentId!!)

            // setting_checked_coupon_list(selectedDocumentId, selectedPosition)

            refresh_select_coupon_recyclerview()
        }

    }

    // 유저 아이디 세팅 메소드
    fun getting_user_id() {
        arguments?.let {
            user_id = PaymentFragmentArgs.fromBundle(it).userId
            user_type = PaymentFragmentArgs.fromBundle(it).userType
            user_name = PaymentFragmentArgs.fromBundle(it).userName
            user_phone_number = PaymentFragmentArgs.fromBundle(it).userPhoneNumber
            order_type = PaymentFragmentArgs.fromBundle(it).orderType
            Log.d("sadjasjd", "getting_user_id: ${order_type}")
            // user_address = PaymentFragmentArgs.fromBundle(it).userAddress
            when (order_type) {
                "cart" -> {
                    // 장바구니 아이템 정보를 가져온다
                    setting_recycledrview_order_cart()
                }
                else -> {
                    setting_recycledrview_order()
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val work1 = async(Dispatchers.IO) {
                    PaymentRepository.found_user_id_by_user_id(user_id)
                }.await()
                if (work1 != true) {
                    fragmentPaymentBinding.tilPaymentNonMemberPassword.visibility = View.VISIBLE
                    fragmentPaymentBinding.tvPaymentCouponList.visibility = View.GONE
                    fragmentPaymentBinding.btnPaymentCouponListShow.visibility = View.GONE
                }
            }

        }

    }

    // textInputLayout의 값을 세팅하고 업데이트 메소드
    fun setting_textInputLayout_delivery_address() {

        // 이름 뷰모델 세팅
        paymentViewModel.til_payment_name_text.value = user_name
        // 전화번호 뷰모델 세팅
        paymentViewModel.til_payment_phone_number_text.value = user_phone_number
        // 주소 뷰모델 세팅
        paymentViewModel.til_payment_address_text.value = user_address
        
        // 이름 옵저버
        paymentViewModel.til_payment_name_text.observe(viewLifecycleOwner) {
            if (it == "null") {
                fragmentPaymentBinding.tilPaymentName.editText?.setText("") // 값을 설정
            }
            else {
                fragmentPaymentBinding.tilPaymentName.editText?.setText(it) // 값을 설정
            }
        }
        // 전화번호 옵저버
        paymentViewModel.til_payment_phone_number_text.observe(viewLifecycleOwner) {
            if (it == "null") {
                fragmentPaymentBinding.tilPaymentPhoneNumber.editText?.setText("")
            }
            else {
                fragmentPaymentBinding.tilPaymentPhoneNumber.editText?.setText(it)
            }
        }
        // 주소 옵저버
        // 초기 배송지의 주소 값을 세팅
        paymentViewModel.til_payment_address_text.observe(viewLifecycleOwner) {
            fragmentPaymentBinding.tilPaymentAddress.editText?.setText(it)
        }
    }

    // 결제 금액 텍스트 세팅
    fun setting_payment_amount_text_observe() {

        paymentViewModel.payment_product_data_list.observe(viewLifecycleOwner) {

            // 총 상품 가격 옵저버
            paymentViewModel.tv_payment_tot_price_text.observe(viewLifecycleOwner) {
                val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                fragmentPaymentBinding.tvPaymentTotPrice.text = formattedPrice
            }

            // 총 할인 가격 옵저버
            paymentViewModel.tv_payment_tot_sale_price_text.observe(viewLifecycleOwner) {
                val formattedPrice = "- " + NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                fragmentPaymentBinding.tvPaymentTotSalePrice.text = formattedPrice
            }

            // 총 합 금액
            paymentViewModel.tv_payment_tot_sum_price_text.observe(viewLifecycleOwner) {
                val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                fragmentPaymentBinding.tvPaymentTotSumPrice.text = formattedPrice
            }

            // 배송비 옵저버
            paymentViewModel.tv_payment_tot_delivery_cost_text.observe(viewLifecycleOwner) {
                val formattedPrice = "+ " + NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                fragmentPaymentBinding.tvPaymentTotDeliveryCost.text = formattedPrice
            }

            // 쿠폰 할인 옵저버
            paymentViewModel.tv_payment_coupon_sale_price_text.observe(viewLifecycleOwner) {
                val formattedPrice = "- " + NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                fragmentPaymentBinding.tvPaymentCouponSalePrice.text = formattedPrice
            }

        }
    }

    // 쿠폰 사용하기 버튼
    fun setting_button() {

        fragmentPaymentBinding.apply {
            btnPaymentCouponListShow.setOnClickListener {

                // 쿠폰 목록 보여주기
                // PaymentCouponBottomSheetFragment 에 데이터 전달
                val bottomSheetFragment = PaymentCouponBottomSheetFragment(this@PaymentFragment). apply {
                    arguments = Bundle().apply {
                        putString("user_id", user_id)
                    }
                }
                bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

            }
        }
    }

    /////////////////////////////////// 주문 목록 recyclerview //////////////////////////////////////
    fun setting_recycledrview_order_cart() {

        // 장바구니에서 체크된 데이터를 가져온다
        viewLifecycleOwner.lifecycleScope.launch {
            val work1 = async(Dispatchers.IO) {
                PaymentRepository.getting_payment_product_by_checked(user_id)
            }
            val result = work1.await()

            result.forEach {
                val cartData = it["checked_cart_data"] as? PaymentProduct
                payment_product_user_id = cartData!!.userId
                cartData.items.forEach {
                    Log.d("1238912389", "cartData : ${it}")
                    payment_product_list = cartData
                }
            }

            Log.d("test1234", "payment_product_list : ${payment_product_list}")

            // 장바구니에서 체크된 데이터에서 가져온 상품 document_id를 통해 상품정보를 가져온다
            val work2 = async(Dispatchers.IO) {
                PaymentRepository.getting_prodcut_by_product_document_id(payment_product_list.items.map { it.productId })
            }.await()

            work2.forEach {
                paymentViewModel.payment_product_data_list.value?.add(it["product_data"] as Product)
            }


            // 결제 금액 텍스트 값 세팅
            var position = 0
            paymentViewModel.payment_product_data_list.value?.forEach {

                // 총 상품 가격 뷰모델 값 세팅
                paymentViewModel.tv_payment_tot_price_text.value =
                    paymentViewModel.tv_payment_tot_price_text.value?.plus(it.price * payment_product_list.items[position].quantity)

                // 총 할인 가격 뷰모델 값 세팅
                paymentViewModel.tv_payment_tot_sale_price_text.value = when(user_type) {
                    "member" -> {
                        paymentViewModel.tv_payment_tot_sale_price_text.value?.plus(
                            (it.price - (it.price * (1 - (it.saleRate / 100.0))).toInt() ) * payment_product_list.items[position].quantity
                        )
                    }
                    else -> {
                        0
                    }
                }

                // 총 합 금액 뷰모델 값 세팅
                paymentViewModel.tv_payment_tot_sum_price_text.value = when(user_type) {
                    "member" -> {
                        paymentViewModel.tv_payment_tot_sum_price_text.value?.plus(
                            ((it.price * (1 - (it.saleRate / 100.0))).toInt()* payment_product_list.items[position].quantity)
                        )
                    }
                    else -> {
                        paymentViewModel.tv_payment_tot_sum_price_text.value?.plus(
                            it.price * payment_product_list.items[position].quantity
                        )
                    }
                }


                position++
            }

            // 총 합 금액에 배송비 추가
            paymentViewModel.tv_payment_tot_sum_price_text.value =
                paymentViewModel.tv_payment_tot_sum_price_text.value?.plus(
                    paymentViewModel.tv_payment_tot_delivery_cost_text.value!!
                )

            Log.d("test1234", "payment_product_data_list : ${paymentViewModel.tv_payment_tot_price_text.value}")

            fragmentPaymentBinding.apply {
                rvPaymentOrderProductList.adapter = PaymentProductAdapter(
                    payment_product_list,
                    paymentViewModel.payment_product_data_list.value!!,
                    paymentViewModel,
                    viewLifecycleOwner,
                    user_type
                )
                rvPaymentOrderProductList.layoutManager = LinearLayoutManager(context)
            }
        }
    }

    fun setting_recycledrview_order() {
        viewLifecycleOwner.lifecycleScope.launch {
            val work1 = async(Dispatchers.IO) {
                payment_product_list = arguments?.let {PaymentFragmentArgs.fromBundle(it).paymentProduct}!!

                PaymentRepository.getting_prodcut_by_product_document_id(payment_product_list.items.map { it.productId })
            }
            val result = work1.await()

            result.forEach {
                paymentViewModel.payment_product_data_list.value?.add(it["product_data"] as Product)
            }

            // 결제 금액 텍스트 값 세팅
            var position = 0
            paymentViewModel.payment_product_data_list.value?.forEach {

                // 총 상품 가격 뷰모델 값 세팅
                paymentViewModel.tv_payment_tot_price_text.value =
                    paymentViewModel.tv_payment_tot_price_text.value?.plus(it.price * payment_product_list.items[position].quantity)

                // 총 할인 가격 뷰모델 값 세팅
                paymentViewModel.tv_payment_tot_sale_price_text.value = when (user_type) {
                    "member" -> {
                        paymentViewModel.tv_payment_tot_sale_price_text.value?.plus(
                            (it.price - (it.price * (1 - (it.saleRate / 100.0))).toInt()) * payment_product_list.items[position].quantity
                        )
                    }

                    else -> {
                        0
                    }
                }

                // 총 합 금액 뷰모델 값 세팅
                paymentViewModel.tv_payment_tot_sum_price_text.value = when (user_type) {
                    "member" -> {
                        paymentViewModel.tv_payment_tot_sum_price_text.value?.plus(
                            ((it.price * (1 - (it.saleRate / 100.0))).toInt() * payment_product_list.items[position].quantity)
                        )
                    }

                    else -> {
                        paymentViewModel.tv_payment_tot_sum_price_text.value?.plus(
                            it.price * payment_product_list.items[position].quantity
                        )
                    }
                }


                position++

            }

            // 총 합 금액에 배송비 추가
            paymentViewModel.tv_payment_tot_sum_price_text.value =
                paymentViewModel.tv_payment_tot_sum_price_text.value?.plus(
                    paymentViewModel.tv_payment_tot_delivery_cost_text.value!!
                )

            Log.d("test1234", "payment_product_data_list : ${paymentViewModel.tv_payment_tot_price_text.value}")

            fragmentPaymentBinding.apply {
                rvPaymentOrderProductList.adapter = PaymentProductAdapter(
                    payment_product_list,
                    paymentViewModel.payment_product_data_list.value!!,
                    paymentViewModel,
                    viewLifecycleOwner,
                    user_type
                )
                rvPaymentOrderProductList.layoutManager = LinearLayoutManager(context)
            }

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////// 선택된 쿠폰 목록 recyclerview ///////////////////////////////////
    fun refresh_select_coupon_recyclerview() {
        viewLifecycleOwner.lifecycleScope.launch {
            val work1 = async(Dispatchers.IO) {
                Log.d("512", "checked_coupon_document_id_list : ${checked_coupon_document_id_list}")
                PaymentRepository.getting_coupon_by_select_coupon_document_id(checked_coupon_document_id_list)
            }
            val coupon_list = work1.await()
            // Log.d("666", "coupon_document_id_list : ${paymentViewModel.select_coupon_list.value?}")

            coupon_list.forEach {
                paymentViewModel.select_coupon_list.value?.add(it["coupon_data"] as Coupon)
            }

            if (coupon_list.size != 0) {

                // 총 합 금액 초기화//////////////////////////////////////////////////////////////////
                var position1 = 0
                paymentViewModel.tv_payment_tot_sum_price_text.value = 0
                paymentViewModel.payment_product_data_list.value?.forEach {
                    // 총 합 금액 뷰모델 값 세팅
                    paymentViewModel.tv_payment_tot_sum_price_text.value =
                        paymentViewModel.tv_payment_tot_sum_price_text.value?.plus(
                            ((it.price * (1 - (it.saleRate / 100.0))).toInt()* payment_product_list.items[position1].quantity)
                        )

                    position1++
                }
                paymentViewModel.tv_payment_tot_sum_price_text.value =
                    paymentViewModel.tv_payment_tot_sum_price_text.value?.plus(paymentViewModel.tv_payment_tot_delivery_cost_text.value!!)
                ////////////////////////////////////////////////////////////////////////////////////

                Log.d("123456", "coupon_document_id_list : ${paymentViewModel.select_coupon_list.value!!}")
                paymentViewModel.select_coupon_list.value!!.forEach {
                    // Log.d("123456", "coupon_document_id_list : ${it.couponType}")


                    when (it.couponType) {
                        // 쿠폰 타입이 할인 금액일 경우
                        CouponType.SALE_PRICE.str -> {
                            // 쿠폰 할인 텍스트
                            paymentViewModel.tv_payment_coupon_sale_price_text.value =
                                // 할인 가격
                                it.salePrice

                        }
                        // 쿠폰 타입이 할인율 일 경우
                        CouponType.SALE_RATE.str -> {
                            // 쿠폰 할인 텍스트
                            paymentViewModel.tv_payment_coupon_sale_price_text.value =
                                    // 총 합 금액 = ( 총합 금액 - 총 배송비 ) x 할인 비율
                                ((paymentViewModel.tv_payment_tot_sum_price_text.value!! -
                                        paymentViewModel.tv_payment_tot_delivery_cost_text.value!!) *
                                        // 할인 비율
                                        ((it.saleRate / 100.0))).toInt()

                        }
                    }

                    var position2 = 0
                    var tot_sum_price = 0
                    // 총 합 금액 초기화
                    paymentViewModel.tv_payment_tot_sum_price_text.value = 0

                    paymentViewModel.payment_product_data_list.value?.forEach {
                        Log.d("123455", "SALE_PRICE : ${it.price}")

                        // 총 합 금액 뷰모델 값 세팅
                        tot_sum_price += (((it.price * (1 - (it.saleRate / 100.0))).toInt() * payment_product_list.items[position2].quantity))

                        position2++
                    }

                    paymentViewModel.tv_payment_tot_sum_price_text.value = tot_sum_price

                    // 총 합 금액 = 총합 금액 + 총 배송비 - 쿠폰 할인
                    paymentViewModel.tv_payment_tot_sum_price_text.value =
                        paymentViewModel.tv_payment_tot_sum_price_text.value!! +
                                paymentViewModel.tv_payment_tot_delivery_cost_text.value!! -
                                paymentViewModel.tv_payment_coupon_sale_price_text.value!!
                    
                }
            }
            else {
                paymentViewModel.tv_payment_coupon_sale_price_text.value = 0
            }

            // Log.d("5", "coupon_document_id_list : ${paymentViewModel.select_coupon_list.value?}")
            fragmentPaymentBinding.rvPaymentSelectCouponList.adapter?.notifyDataSetChanged()
        }
    }

    fun setting_recyclerview_select_coupon() {
        fragmentPaymentBinding.apply {
            rvPaymentSelectCouponList.adapter = SelectCouponAdapter(
                paymentViewModel.select_coupon_list.value!!,
                this@PaymentFragment,
                viewLifecycleOwner
            )
            rvPaymentSelectCouponList.layoutManager = LinearLayoutManager(context)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

}