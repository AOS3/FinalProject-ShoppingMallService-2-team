package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import com.nemodream.bangkkujaengi.customer.data.repository.NonMemberOrderCheckRepository
import com.nemodream.bangkkujaengi.databinding.FragmentNonMemberOrderCheckBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NonMemberOrderCheckFragment : Fragment() {

    lateinit var fragmentNonMemberOrderCheckBinding: FragmentNonMemberOrderCheckBinding

    lateinit var non_member_purchase_lsit: List<Purchase>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentNonMemberOrderCheckBinding = FragmentNonMemberOrderCheckBinding.inflate(inflater, container, false)

        setting_btn_order_inquiry()

        setting_tb_non_member_inquiry()

        return fragmentNonMemberOrderCheckBinding.root
    }

    fun setting_tb_non_member_inquiry() {
        fragmentNonMemberOrderCheckBinding.toolbarNonMemberInquiry.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    // 주문 조회 버튼
    fun setting_btn_order_inquiry() {
        fragmentNonMemberOrderCheckBinding.btnOrderInquiry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val work1 = async(Dispatchers.IO) {
                    val receiverPhone = fragmentNonMemberOrderCheckBinding.tfOrderInquiryMemberNumber.editText?.text.toString()
                    val nonMemberPassword = fragmentNonMemberOrderCheckBinding.tfOrderInquiryMemberPassword.editText?.text.toString()
                    NonMemberOrderCheckRepository.getting_non_member_order_check_data_by_phone_num(receiverPhone, nonMemberPassword)
                }

                non_member_purchase_lsit = work1.await()

                if (non_member_purchase_lsit == emptyList<Purchase>()) {
                    // 빈 리스트일 경우 토스트 메시지 표시
                    showCustomToast("일치하는 주문이 없습니다.", R.drawable.ic_symbol)
                }
                else {
                    Log.d("non_member_purchase_lsit", "$non_member_purchase_lsit")
                    val action = NonMemberOrderCheckFragmentDirections
                        .actionNonMemberOrderCheckFragmentToNonMemberOrderHistoryFragment(
                            non_member_purchase_lsit.toTypedArray() // List → Array 변환
                        )
                    findNavController().navigate(action)
                }

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

}