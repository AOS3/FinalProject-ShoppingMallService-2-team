package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import com.nemodream.bangkkujaengi.customer.ui.adapter.NonMemberOrderHistoryProductAdapter
import com.nemodream.bangkkujaengi.databinding.FragmentNonMemberOrderHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class NonMemberOrderHistoryFragment : Fragment() {

    lateinit var fragmentNonMemberOrderHistoryBinding: FragmentNonMemberOrderHistoryBinding

    lateinit var purchaseList: List<Purchase>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentNonMemberOrderHistoryBinding = FragmentNonMemberOrderHistoryBinding.inflate(inflater, container, false)

        setting_non_member_purchase_list()

        setting_recycler_view()

        setting_text()

        setting_tb_non_member_inquiry()

        return fragmentNonMemberOrderHistoryBinding.root
    }

    fun setting_tb_non_member_inquiry() {
        fragmentNonMemberOrderHistoryBinding.tbNonMemberOrderHistory.setNavigationOnClickListener {
            // navigation_my_page가 MyPageFragment의 ID입니다.
            // popBackStack으로 MyPageFragment까지의 모든 프래그먼트를 스택에서 제거합니다.
            findNavController().popBackStack(R.id.navigation_my_page, false)
        }
    }

    fun setting_non_member_purchase_list() {
        // Safe Args를 통해 전달된 데이터 가져오기
        val args = NonMemberOrderHistoryFragmentArgs.fromBundle(requireArguments())
        purchaseList = args.purchaseList.toList() // Array → List 변환

        purchaseList.forEach {
            Log.d("ReceivedData", "Purchase: $it")
        }
    }

    fun setting_text() {
        fragmentNonMemberOrderHistoryBinding.tvRowNonMemberOrderDate.text = convertDateFormat(
            purchaseList[0].purchaseDateTime.toString()
        )
    }

    // yyyyMMddHHmmss 형식의 날짜를 yyyy년 MM월 dd일 HH:mm:ss로 변환하는 메소드
    fun convertDateFormat(rawDate: String): String {
        return try {
            // 입력 형식 정의
            val inputFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            // 출력 형식 정의
            val outputFormat = SimpleDateFormat("yyyy.MM.dd / HH:mm", Locale.getDefault())

            // 입력 날짜 파싱 후 새로운 형식으로 변환
            val date = inputFormat.parse(rawDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            rawDate // 변환 실패 시 원본 반환
        }
    }

    fun setting_recycler_view() {
        fragmentNonMemberOrderHistoryBinding.apply {
            rvNonMemberOrderHistoryList.adapter = NonMemberOrderHistoryProductAdapter(
                purchaseList,
                viewLifecycleOwner,
                this@NonMemberOrderHistoryFragment
            )

            rvNonMemberOrderHistoryList.layoutManager = LinearLayoutManager(context)
        }
    }



}