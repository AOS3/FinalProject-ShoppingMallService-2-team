package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.repository.OrderHistoryRepository
import com.nemodream.bangkkujaengi.customer.ui.adapter.OrderDetailsAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.OrderDetailsViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentOrderDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OrderDetailsFragment : Fragment() {

    lateinit var fragmentOrderDetailsBinding : FragmentOrderDetailsBinding

    val orderDetailsViewModel : OrderDetailsViewModel by viewModels()

    // 유저 id
    var user_id = ""
    // 상품 구매 시간 (년, 월, 일, 시, 분, 초)
    var purchaseDateTime = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentOrderDetailsBinding = FragmentOrderDetailsBinding.inflate(inflater, container, false)

        // 툴바 세팅
        setting_toolbar()
        
        // 값 옵저버 세팅
        setting_observe()

        // 유저 id와 상품 구매 시간을 가져오는 메소드
        setting_userId_perchaseDateTime()

        // 상품 목록을 보여주는 recyclerview 세팅
        setting_order_details_recyclerview()

        return fragmentOrderDetailsBinding.root
    }

    // 툴바 세팅
    fun setting_toolbar() {
        fragmentOrderDetailsBinding.tbOrderDetails.apply {
            // 툴바에 뒤로가기 버튼 아이콘 생성
            setNavigationIcon(R.drawable.ic_arrow_back)
            // 툴바 뒤로가기 버튼의 이벤트
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    // 유저 id와 상품 구매 시간을 가져오는 메소드
    fun setting_userId_perchaseDateTime() {
        arguments?.let {
            user_id = OrderDetailsFragmentArgs.fromBundle(it).userId
            purchaseDateTime = OrderDetailsFragmentArgs.fromBundle(it).purchaseDateTime
        }
    }

    // 상품 목록을 보여주는 recyclerview 세팅
    fun setting_order_details_recyclerview() {

        viewLifecycleOwner.lifecycleScope.launch {
            val work1 = async(Dispatchers.IO) {
                OrderHistoryRepository.getting_order_history_list_by_userId_purchaseDate(user_id, purchaseDateTime)
            }
            orderDetailsViewModel.order_history_product_list.value = work1.await()

            orderDetailsViewModel.order_history_product_list.value!!.forEach {
                Log.d("OrderDetailsFragment3", "order_history_product_list : $it")
            }

            // 가격 값 세팅
            setting_text()

            fragmentOrderDetailsBinding.apply {
                Log.d("OrderDetailsFragment", "user_id : $user_id , purchaseDateTime : $purchaseDateTime")
                rvOrderDetailsProductList.adapter = OrderDetailsAdapter(
                    orderDetailsViewModel,
                    viewLifecycleOwner
                )

                rvOrderDetailsProductList.layoutManager = LinearLayoutManager(context)
            }
        }

    }

    // 가격 값 세팅
    fun setting_text() {
        orderDetailsViewModel.order_history_product_list.value!!.forEach {

            // 총 상품 가격
            orderDetailsViewModel.tot_product_price.value =
                orderDetailsViewModel.tot_product_price.value!!.plus(it.productCost)

            // 총 할인 가격
            orderDetailsViewModel.tot_sale_price.value =
                orderDetailsViewModel.tot_sale_price.value!!.plus(
                    it.productCost - it.totPrice
                )

            // 쿠폰 할인
            orderDetailsViewModel.sale_coupon_price.value =
                it.couponSalePrice

            // 총 배송비
            orderDetailsViewModel.tot_delivery_price.value =
                it.deliveryCost
        }

        // 총 결제 금액
        orderDetailsViewModel.tot_payment_price.value =
                    orderDetailsViewModel.tot_product_price.value!! -
                    orderDetailsViewModel.tot_sale_price.value!! -
                    orderDetailsViewModel.sale_coupon_price.value!! +
                    orderDetailsViewModel.tot_delivery_price.value!!
    }

    // 값 옵저버 세팅
    fun setting_observe() {
        fragmentOrderDetailsBinding.apply {

            // 총 상품 가격 옵저버
            orderDetailsViewModel.tot_product_price.observe(viewLifecycleOwner) {
                val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                tvOrderDetailsTotPrice.text = formattedPrice
            }

            // 총 할인 가격 옵저버
            orderDetailsViewModel.tot_sale_price.observe(viewLifecycleOwner) {
                val formattedPrice = "- " + NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                tvOrderDetailsTotSalePrice.text = formattedPrice
            }

            // 쿠폰 할인 옵저버
            orderDetailsViewModel.sale_coupon_price.observe(viewLifecycleOwner) {
                val formattedPrice = "- " + NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                tvOrderDetailsCouponSalePrice.text = formattedPrice
            }

            // 총 배송비 옵저버
            orderDetailsViewModel.tot_delivery_price.observe(viewLifecycleOwner) {
                val formattedPrice = "+ " + NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                tvOrderDetailsTotDeliveryCost.text = formattedPrice
            }

            // 총 결제 금액
            orderDetailsViewModel.tot_payment_price.observe(viewLifecycleOwner) {
                val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
                tvOrderDetailsTotSumPrice.text = formattedPrice
            }


        }
    }

}