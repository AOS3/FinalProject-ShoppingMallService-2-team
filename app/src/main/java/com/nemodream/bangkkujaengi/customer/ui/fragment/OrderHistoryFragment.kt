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
import com.nemodream.bangkkujaengi.customer.data.repository.ShoppingCartRepository
import com.nemodream.bangkkujaengi.customer.ui.adapter.OrderHistoryAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.OrderHistoryViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentOrderHistoryBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.getUserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class OrderHistoryFragment : Fragment() {

    lateinit var fragmentOrderHistoryBinding: FragmentOrderHistoryBinding

    val orderHistoryViewModel : OrderHistoryViewModel by viewModels()

    // 유저 id
    var user_type = ""
    var user_id = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentOrderHistoryBinding = FragmentOrderHistoryBinding.inflate(inflater, container, false)

        // 유저 id 세팅
        setting_user_id()

        // 툴바 세팅
        setting_toolbar()
        
        // 주문 내역 리사이클러뷰 세팅
        // setting_order_history_recyclerview()

        return fragmentOrderHistoryBinding.root
    }

    // 툴바 세팅
    fun setting_toolbar() {
        fragmentOrderHistoryBinding.tbOrderHistory.apply {
            // 툴바에 뒤로가기 버튼 아이콘 생성
            setNavigationIcon(R.drawable.ic_arrow_back)
            // 툴바 뒤로가기 버튼의 이벤트
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    // 유저 id 세팅
    fun setting_user_id() {
        user_type = requireContext().getUserType()
        Log.d("test1213", "setting_user_id: ${user_type}")

        when(user_type) {
            "member" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val work1 = async(Dispatchers.IO) {
                        ShoppingCartRepository.getting_user_id_by_document_id(requireContext().getUserId())
                    }
                    user_id = work1.await()
                    Log.d("test1213", "setting_user_id: ${user_id}")
                    setting_order_history_recyclerview()
                }
            }
            "guest" -> {
                user_id = requireContext().getUserId()
                Log.d("test1213", "setting_user_id: ${user_id}")
                setting_order_history_recyclerview()
            }
            else -> {
                ""
            }
        }
    }

    // 날짜별로 묶는 recyclerview ////////////////////////////////////////////////////////////////////

    fun setting_order_history_recyclerview() {

        viewLifecycleOwner.lifecycleScope.launch {
            val work1 = async(Dispatchers.IO) {
                OrderHistoryRepository.getting_order_history_list(user_id)
            }
            orderHistoryViewModel.order_history_product_list.value = work1.await()

            orderHistoryViewModel.order_history_product_list.value!!.forEach {
                Log.d("orderHistory", "orderHistory: ${it}")
            }


            // purchaseDate에서 날짜(년-월-일)만 추출하여 중복 제거 및 정렬
            val dateTimeFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            orderHistoryViewModel.order_history_date_list.value = orderHistoryViewModel.order_history_product_list.value!!
                .mapNotNull { it.purchaseDate?.toDate() } // Timestamp를 Date로 변환
                .map { dateTimeFormatter.format(it).toLong() } // 날짜 형식으로 변환 (yyyy-MM-dd)
                .distinct() // 중복 제거
                .sortedDescending() // 내림차순 정렬

            orderHistoryViewModel.order_history_date_list.value!!.forEach {
                Log.d("uniqueTimes", "uniqueTimes: ${it}")
            }

            fragmentOrderHistoryBinding.apply {
                rvOrderHistoryList.adapter = OrderHistoryAdapter(
                    this@OrderHistoryFragment,
                    orderHistoryViewModel,
                    viewLifecycleOwner
                )
                rvOrderHistoryList.layoutManager = LinearLayoutManager(context)
            }
        }
    }

}