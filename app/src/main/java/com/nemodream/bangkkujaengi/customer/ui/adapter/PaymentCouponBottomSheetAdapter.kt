package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import com.nemodream.bangkkujaengi.customer.data.model.CouponType
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.PaymentCouponBottomSheetViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.RowPaymentCouponRecyclerviewViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentPaymentCouponBottomeSheetBinding
import com.nemodream.bangkkujaengi.databinding.RowPaymentCouponRecyclerviewBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PaymentCouponBottomSheetAdapter(
    val fragmentPaymentCouponBottomSheetBinding: FragmentPaymentCouponBottomeSheetBinding,
    val paymentCouponBottomSheetViewModel: PaymentCouponBottomSheetViewModel,
    val coupon_list : MutableList<Coupon>,
    val viewLifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<PaymentCouponBottomSheetAdapter.PaymentCouponBottomSheetViewHolder>() {

    inner class PaymentCouponBottomSheetViewHolder(val rowPaymentCouponRecyclerviewBinding: RowPaymentCouponRecyclerviewBinding) :
            RecyclerView.ViewHolder(rowPaymentCouponRecyclerviewBinding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentCouponBottomSheetViewHolder {
        val rowPaymentCouponRecyclerviewBinding = RowPaymentCouponRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PaymentCouponBottomSheetViewHolder(rowPaymentCouponRecyclerviewBinding)
    }

    override fun getItemCount(): Int {
        return coupon_list.size
    }

    override fun onBindViewHolder(holder: PaymentCouponBottomSheetViewHolder, position: Int) {
        val rowPaymentCouponRecyclerviewViewModel = RowPaymentCouponRecyclerviewViewModel()

        // 옵저버 세팅 메소드 호출
        setting_text_observe(
            holder.rowPaymentCouponRecyclerviewBinding,
            rowPaymentCouponRecyclerviewViewModel,
            position
        )

        // 뷰모델 초기 값 세팅 메소드 호출
        setting_viewmodel_value(
            rowPaymentCouponRecyclerviewViewModel,
            holder.rowPaymentCouponRecyclerviewBinding,
            position
        )

        // 쿠폰 하나만 선택되도록 하기 위한 메소드 호출
        select_single_coupon(holder, paymentCouponBottomSheetViewModel, position)


        if (paymentCouponBottomSheetViewModel.checked_document_id.value == coupon_list[position].documentId) {
            holder.rowPaymentCouponRecyclerviewBinding.cvRowPaymentCoupon.isChecked = true
        }

    }

    // 옵저버 세팅 메소드
    fun setting_text_observe(
        rowPaymentCouponRecyclerviewBinding: RowPaymentCouponRecyclerviewBinding,
        rowPaymentCouponRecyclerviewViewModel: RowPaymentCouponRecyclerviewViewModel,
        position: Int
    ) {
        rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_title.observe(viewLifecycleOwner) {
            rowPaymentCouponRecyclerviewBinding.tvRowPaymentCouponTitle.text = it
        }

        if(coupon_list[position].couponType == CouponType.SALE_RATE.str) {
            rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_rate.observe(viewLifecycleOwner) {
                rowPaymentCouponRecyclerviewBinding.tvRowPaymentCouponPrice.text = it.toString() + "%"
            }
        }
        else {
            rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_price.observe(viewLifecycleOwner) {
                val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + "원"
                rowPaymentCouponRecyclerviewBinding.tvRowPaymentCouponPrice.text = formattedPrice
            }
        }

        rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_condition.observe(viewLifecycleOwner) {
            rowPaymentCouponRecyclerviewBinding.tvRowPaymentCouponCondition.text = it
        }

        rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_end_date.observe(viewLifecycleOwner) {
            val date = it.toDate().time // Timestamp를 Date로 변환
            val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul") // 한국 시간대 설정
            rowPaymentCouponRecyclerviewBinding.tvRowPaymentCouponPeriod.text = "~" + simpleDateFormat.format(date)
        }

    }

    // 뷰모델 초기 값 세팅 메소드
    fun setting_viewmodel_value(
        rowPaymentCouponRecyclerviewViewModel: RowPaymentCouponRecyclerviewViewModel,
        rowPaymentCouponRecyclerviewBinding: RowPaymentCouponRecyclerviewBinding,
        position: Int
    ) {
        rowPaymentCouponRecyclerviewBinding.apply {

            // 쿠폰 제목 뷰모델 세팅
            rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_title.value =
                coupon_list[position].title

            // 쿠폰 할인 가격 뷰모델 세팅
            rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_price.value =
                coupon_list[position].salePrice

            // 쿠폰 할인률 뷰모델 세팅
            rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_rate.value =
                coupon_list[position].saleRate

            // 쿠폰 조건 뷰모델 세팅
            rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_condition.value =
                coupon_list[position].conditionDescription

            // 쿠폰 ~까지 뷰모델 세팅
            rowPaymentCouponRecyclerviewViewModel.tv_row_payment_coupon_end_date.value =
                coupon_list[position].endCouponDate

        }
    }

    // 쿠폰 하나만 선택되도록 하기 위한 메소드
    fun select_single_coupon(
        holder: PaymentCouponBottomSheetViewHolder,
        paymentCouponBottomSheetViewModel: PaymentCouponBottomSheetViewModel,
        position: Int,
        ) {
        paymentCouponBottomSheetViewModel.checked_position.observe(viewLifecycleOwner) {
            if (it == position) {
                holder.rowPaymentCouponRecyclerviewBinding.cvRowPaymentCoupon.isChecked = true
            } else {
                holder.rowPaymentCouponRecyclerviewBinding.cvRowPaymentCoupon.isChecked = false
            }
        }

        holder.rowPaymentCouponRecyclerviewBinding.cvRowPaymentCoupon.setOnClickListener {
            holder.rowPaymentCouponRecyclerviewBinding.cvRowPaymentCoupon.isChecked = false
            val isChecked = !holder.rowPaymentCouponRecyclerviewBinding.cvRowPaymentCoupon.isChecked
            holder.rowPaymentCouponRecyclerviewBinding.cvRowPaymentCoupon.isChecked = isChecked
            if (isChecked) {
                paymentCouponBottomSheetViewModel.checked_position.value = position
                paymentCouponBottomSheetViewModel.checked_document_id.value = coupon_list[position].documentId
            } else {
                paymentCouponBottomSheetViewModel.checked_position.value = null
            }

            paymentCouponBottomSheetViewModel.bottom_sheet_show.value = false
        }
    }


}