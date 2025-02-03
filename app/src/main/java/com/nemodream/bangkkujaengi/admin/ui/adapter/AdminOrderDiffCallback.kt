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
        return oldList[oldItemPosition].orderNumber == newList[newItemPosition].orderNumber
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}