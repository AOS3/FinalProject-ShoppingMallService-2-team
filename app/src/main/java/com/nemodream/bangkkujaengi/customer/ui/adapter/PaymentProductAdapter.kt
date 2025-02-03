package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.PaymentProduct
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.repository.ShoppingCartRepository
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.PaymentViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.RowPaymentRecyclerviewViewModel
import com.nemodream.bangkkujaengi.databinding.RowPaymentRecyclerviewBinding
import com.nemodream.bangkkujaengi.utils.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PaymentProductAdapter(
    // 장바구니에서 체크표시한 상품의 갯수, 상품 문서 id 등을 담는 변수
    val payment_product_list: PaymentProduct,
    // 결제할 상품 정보를 담을 리스트
    var payment_product_data_list: MutableList<Product>,
    // 뷰모델
    var paymentViewModel: PaymentViewModel,
    // observe
    val viewLifecycleOwner: LifecycleOwner,
    // 회원 or 비회원
    val user_type: String
    ) : RecyclerView.Adapter<PaymentProductAdapter.PaymentProductViewHolder>() {

    inner class PaymentProductViewHolder(val rowPaymentRecyclerviewBinding: RowPaymentRecyclerviewBinding) :
        RecyclerView.ViewHolder(rowPaymentRecyclerviewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentProductViewHolder {
        val rowPaymentRecyclerviewBinding = RowPaymentRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PaymentProductViewHolder(rowPaymentRecyclerviewBinding)
    }

    override fun getItemCount(): Int = payment_product_data_list.size

    override fun onBindViewHolder(holder: PaymentProductViewHolder, position: Int) {
        val rowPaymentRecyclerviewViewModel = RowPaymentRecyclerviewViewModel()

        when(user_type) {
            "member" -> {

            }
            else -> {
                holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductOriginPrice.visibility = View.GONE
                holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductSalePercent.visibility = View.GONE
            }
        }

        // 옵저버 세팅 메소드 호출
        setting_text_observe(rowPaymentRecyclerviewViewModel, holder, position)

        // 뷰모델 초기 값 세팅 메소드 호출
        setting_viewmodel_value(rowPaymentRecyclerviewViewModel, position)

        // 이미지 로드 메소드 호출
        setting_image_load(holder, position)

    }

    // 이미지 로드 메소드
    fun setting_image_load(holder: PaymentProductViewHolder, position: Int) {
        // 상품 이미지 로드
        viewLifecycleOwner.lifecycleScope.launch {
            val work1 = async(Dispatchers.IO) {
                ShoppingCartRepository.getting_image(payment_product_data_list[position].images[0])
            }
            val uri_image = work1.await()

            holder.rowPaymentRecyclerviewBinding.ivRowPaymentProductImage.loadImage(uri_image)
        }
    }

    // 뷰모델 초기 값 세팅 메소드
    fun setting_viewmodel_value(rowPaymentRecyclerviewViewModel: RowPaymentRecyclerviewViewModel, position: Int) {
        
        // 상품명 뷰모델 값 세팅
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_name.value = payment_product_data_list[position].productName

        // 할인 전 가격 뷰모델 값 세팅
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_origin_price.value =
            payment_product_data_list[position].price * payment_product_list.items[position].quantity

        // 할인 비율 뷰모델 값 세팅
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_sale_percent.value =
            payment_product_data_list[position].saleRate

        // 할인 후 가격 뷰모델 값 세팅
        // 원가
        val originalPrice = payment_product_data_list[position].price
        // 할인율
        val discountRate = payment_product_data_list[position].saleRate
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_sale_price.value = when(user_type) {
            "member" -> {
                ((originalPrice * (1 - (discountRate / 100.0))).toInt() * payment_product_list.items[position].quantity)
            }
            else -> {
                payment_product_data_list[position].price * payment_product_list.items[position].quantity
            }
        }

        // 상품 갯수 뷰모델 값 세팅
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_cnt.value =
            payment_product_list.items[position].quantity


    }

    // 옵저버 세팅 메소드
    fun setting_text_observe(
        rowPaymentRecyclerviewViewModel: RowPaymentRecyclerviewViewModel,
        holder: PaymentProductViewHolder,
        position: Int
    ) {

        // 상품 이름 옵저버
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_name.observe(viewLifecycleOwner) {
            holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductName.text = it
        }

        if (payment_product_list.items[position].color == "") {
            holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductOption.text =
                "옵션 : 없음"
        }
        else {
            holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductOption.text =
                "옵션 : " + payment_product_list.items[position].color
        }


        // 상품 할인 전 가격 옵저버
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_origin_price.observe(viewLifecycleOwner) {
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
            holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductOriginPrice.apply {
                text = formattedPrice
                // Paint.STRIKE_THRU_TEXT_FLAG : 텍스트에 사선을 추가하는 플래그
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // 텍스트에 사선 추가
            }
        }

        // 할인 비율 옵저버
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_sale_percent.observe(viewLifecycleOwner) {
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " %"
            holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductSalePercent.text = formattedPrice
        }

        // 상품 할인 후 가격 옵저버
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_sale_price.observe(viewLifecycleOwner) {
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
            holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductSalePrice.text = formattedPrice
        }

        // 상품 갯수 옵저버
        rowPaymentRecyclerviewViewModel.tv_row_payment_product_cnt.observe(viewLifecycleOwner) {
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + "개"
            holder.rowPaymentRecyclerviewBinding.tvRowPaymentProductCnt.text = formattedPrice
        }


    }

}