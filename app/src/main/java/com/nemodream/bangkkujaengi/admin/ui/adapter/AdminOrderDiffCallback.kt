package com.nemodream.bangkkujaengi.admin.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.nemodream.bangkkujaengi.admin.data.model.Order

class AdminOrderDiffCallback(
    private val oldList: List<Order>,
    private val newList: List<Order>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // 문서 ID를 기준으로 동일 항목인지 확인
        return oldList[oldItemPosition].documentId == newList[newItemPosition].documentId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // 항목의 내용이 동일한지 확인
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}