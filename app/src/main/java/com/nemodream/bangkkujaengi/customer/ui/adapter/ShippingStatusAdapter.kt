package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ShippingStatusViewModel
import com.nemodream.bangkkujaengi.databinding.RowShippingStatusDetailsRecyclerviewBinding

class ShippingStatusAdapter(
    val shippingStatusViewModel: ShippingStatusViewModel
) : RecyclerView.Adapter<ShippingStatusAdapter.ShippingStatusViewHolder>() {

    inner class ShippingStatusViewHolder(val rowShippingStatusDetailsRecyclerviewBinding: RowShippingStatusDetailsRecyclerviewBinding) :
            RecyclerView.ViewHolder(rowShippingStatusDetailsRecyclerviewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShippingStatusViewHolder {
        val rowShippingStatusDetailsRecyclerviewBinding = RowShippingStatusDetailsRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ShippingStatusViewHolder(rowShippingStatusDetailsRecyclerviewBinding)
    }

    override fun getItemCount(): Int = shippingStatusViewModel.shipping_status_list.value!!.trackingDetails.size

    override fun onBindViewHolder(holder: ShippingStatusViewHolder, position: Int) {

        // 텍스트 세팅
        setting_text(holder.rowShippingStatusDetailsRecyclerviewBinding, position)

    }

    // 텍스트 세팅
    fun setting_text(
        rowShippingStatusDetailsRecyclerviewBinding : RowShippingStatusDetailsRecyclerviewBinding,
        position: Int
    ) {
        // 배송 상세 내역 데이터
        val detail = shippingStatusViewModel.shipping_status_list.value!!.trackingDetails[position]

        // 배송 위치와 상태를 나타내는 문자열
        val formattedDetail = "${detail.where} (${detail.kind})"
        rowShippingStatusDetailsRecyclerviewBinding.tvShippingStatusText.text = formattedDetail

        // 상세 내역 날짜와 시간 분리
        val (date, time) = detail.timeString.split(" ")
        rowShippingStatusDetailsRecyclerviewBinding.tvShippingStatusTextDate.text = date
        rowShippingStatusDetailsRecyclerviewBinding.tvShippingStatusTextTime.text = time


    }

}