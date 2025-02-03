package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nemodream.bangkkujaengi.customer.data.model.Coupon
import com.nemodream.bangkkujaengi.customer.data.repository.PaymentRepository
import com.nemodream.bangkkujaengi.customer.ui.adapter.PaymentCouponBottomSheetAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.PaymentCouponBottomSheetViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.PaymentViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentPaymentCouponBottomeSheetBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PaymentCouponBottomSheetFragment(val paymentFragment: PaymentFragment) : BottomSheetDialogFragment() {

    lateinit var fragmentPaymentCouponBottomSheetBinding: FragmentPaymentCouponBottomeSheetBinding

    val paymentCouponBottomSheetViewModel: PaymentCouponBottomSheetViewModel by viewModels()

    lateinit var paymentViewModel: PaymentViewModel

    lateinit var coupon_document_id_list: List<String>

    lateinit var coupon_list : MutableList<Coupon>

    val testData = Array(10) {
        "쿠폰명 $it"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentPaymentCouponBottomSheetBinding = FragmentPaymentCouponBottomeSheetBinding.inflate(inflater, container, false)

        paymentCouponBottomSheetViewModel.user_id.value = arguments?.getString("user_id")
        if (paymentFragment.checked_coupon_document_id_list.size != 0) {
            paymentCouponBottomSheetViewModel.checked_document_id.value =
                paymentFragment.checked_coupon_document_id_list[0]
        }
        Log.d("1818", "selected_document_id : ${paymentCouponBottomSheetViewModel.checked_document_id.value}")

        Log.d("1234", "user_id : ${paymentCouponBottomSheetViewModel.user_id.value}")

        // 쿠폰 항목을 클릭시 BottomSheet 닫기
        paymentCouponBottomSheetViewModel.bottom_sheet_show.observe(viewLifecycleOwner) {
            if (!it) {
                paymentCouponBottomSheetViewModel.checked_position.value
                val bundle = Bundle().apply {
                    putInt("select_position", paymentCouponBottomSheetViewModel.checked_position.value!!)
                    putString("select_document_id", paymentCouponBottomSheetViewModel.checked_document_id.value)
                }

                Log.d("12345", "select_position : ${paymentCouponBottomSheetViewModel.checked_position.value}")
                Log.d("12345", "select_document_id : ${paymentCouponBottomSheetViewModel.checked_document_id.value}")

                parentFragmentManager.setFragmentResult("couponResultKey", bundle)

                dismiss()
            }
        }

        // 쿠폰 목록 recyclerview 설정 메소드 호출
        setting_recyclerview()

        return fragmentPaymentCouponBottomSheetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BottomSheet의 Behavior 가져오기
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // BottomSheet 높이 고정
        bottomSheet.layoutParams.height = 1500 // 원하는 높이 (예: 1500dp)
        behavior.isFitToContents = true // 내용 크기에 따라 크기 변하지 않도록 설정
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // 항상 확장 상태 유지
    }

    // 쿠폰 목록 recyclerview 설정
    fun setting_recyclerview() {

        viewLifecycleOwner.lifecycleScope.launch {
            val work1 = async(Dispatchers.IO) {
                PaymentRepository.getting_coupon_document_id_by_user_id(paymentCouponBottomSheetViewModel.user_id.value!!)
            }
            coupon_document_id_list = work1.await()
            Log.d("1234", "coupon_document_id_list : ${coupon_document_id_list}")

            val work2 = async(Dispatchers.IO) {
                PaymentRepository.getting_coupon_all(coupon_document_id_list)
            }
            coupon_list = work2.await()

            coupon_list.forEach {
                Log.d("1234", "coupon_list : ${it.documentId}")
            }

            fragmentPaymentCouponBottomSheetBinding.apply {
                rvPaymentCouponList.adapter = PaymentCouponBottomSheetAdapter(
                    fragmentPaymentCouponBottomSheetBinding,
                    paymentCouponBottomSheetViewModel,
                    coupon_list,
                    viewLifecycleOwner
                )
                rvPaymentCouponList.layoutManager = LinearLayoutManager(context)
            }

        }
    }
}