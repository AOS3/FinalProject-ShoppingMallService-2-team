package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.Purchase
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseState
import com.nemodream.bangkkujaengi.customer.data.repository.ShoppingCartRepository
import com.nemodream.bangkkujaengi.customer.deliverytracking.RetrofitInstance
import com.nemodream.bangkkujaengi.customer.ui.fragment.NonMemberOrderHistoryFragment
import com.nemodream.bangkkujaengi.customer.ui.fragment.NonMemberOrderHistoryFragmentDirections
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.NonMemberOrderHistoryProductViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.OrderHistoryProductAdapterViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentNonMemberOrderHistoryBinding
import com.nemodream.bangkkujaengi.databinding.RowNonMemberOrderHistoryProductBinding
import com.nemodream.bangkkujaengi.databinding.RowOrderHistoryProductBinding
import com.nemodream.bangkkujaengi.utils.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.text.NumberFormat
import java.util.Locale

class NonMemberOrderHistoryProductAdapter(
    val purchaseList: List<Purchase>,
    val viewLifecycleOwner : LifecycleOwner,
    val nonMemberOrderHistoryFragment: NonMemberOrderHistoryFragment
) : RecyclerView.Adapter<NonMemberOrderHistoryProductAdapter.NonMemberOrderHistoryProductViewHolder>() {

    inner class NonMemberOrderHistoryProductViewHolder(val rowNonMemberOrderHistoryProductBinding: RowNonMemberOrderHistoryProductBinding) :
        RecyclerView.ViewHolder(rowNonMemberOrderHistoryProductBinding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NonMemberOrderHistoryProductViewHolder {
        val rowNonMemberOrderHistoryProductBinding = RowNonMemberOrderHistoryProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return NonMemberOrderHistoryProductViewHolder(rowNonMemberOrderHistoryProductBinding)
    }

    override fun getItemCount(): Int = purchaseList.size

    override fun onBindViewHolder(holder: NonMemberOrderHistoryProductViewHolder, position: Int) {
        val nonMemberOrderHistoryProductViewModel = NonMemberOrderHistoryProductViewModel()

        fetchTrackingInfo("04", purchaseList[position].purchaseInvoiceNumber.toString(), nonMemberOrderHistoryProductViewModel, position)
        // 텍스트 세팅
        setting_text(holder.rowNonMemberOrderHistoryProductBinding, nonMemberOrderHistoryProductViewModel, position)
        // 상품 리스트 이미지 세팅
        setting_product_list_image(holder.rowNonMemberOrderHistoryProductBinding, position)
        // 배송 조회 버튼 이벤트
        setting_btn_row_non_member_order_history_product_shipping_status_check(
            holder.rowNonMemberOrderHistoryProductBinding,
            nonMemberOrderHistoryProductViewModel,
            position
        )

    }

    // 배송 조회 버튼 이벤트
    fun setting_btn_row_non_member_order_history_product_shipping_status_check(
        rowNonMemberOrderHistoryProductBinding: RowNonMemberOrderHistoryProductBinding,
        nonMemberOrderHistoryProductViewModel: NonMemberOrderHistoryProductViewModel,
        position: Int
    ) {
        rowNonMemberOrderHistoryProductBinding.btnRowNonMemberOrderHistoryProductShippingStatusCheck.setOnClickListener {
            Log.d("orderHistoryProductAdapter3123", "order_history_product_list: ${purchaseList[position].documentId}")
            val action = NonMemberOrderHistoryFragmentDirections.actionNonMemberOrderHistoryFragmentToShippingStatusFragment(
                purchaseList[position].documentId,
                purchaseList[position].purchaseDateTime,
                nonMemberOrderHistoryProductViewModel.shipping_status_list.value
            )
            nonMemberOrderHistoryFragment.findNavController().navigate(action)
        }
    }

    // 텍스트 세팅
    fun setting_text(
        holder: RowNonMemberOrderHistoryProductBinding,
        nonMemberOrderHistoryProductViewModel: NonMemberOrderHistoryProductViewModel,
        position: Int
    ) {
        // 상품명
        holder.tvRowNonMemberOrderHistoryProductProductName.text = purchaseList[position].productTitle
        // 상품 색깔
        holder.tvRowNonMemberOrderHistoryProductProductOption.text = purchaseList[position].color
        // 상품 가격
        val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(
            purchaseList[position].totPrice
        ) + " 원"
        holder.tvRowNonMemberOrderHistoryProductPrice.text = formattedPrice
        // 상품 수량
        holder.tvRowNonMemberOrderHistoryProductCnt.text = purchaseList[position].purchaseQuantity.toString()

        nonMemberOrderHistoryProductViewModel.shipping_status_list.observe(viewLifecycleOwner) {
            holder.tvRowNonMemberOrderHistoryProductDeliveryState.text = when(it.level) {
                1 -> "배송 준비 중"
                2,3,4,5 -> "배송 중"
                6 -> "배송 완료"
                else -> "결제 완료"
            }
        }
    }

    // api를 통해 배송 상세 내용을 저장
    fun fetchTrackingInfo(courierCode: String, invoiceNumber: String, nonMemberOrderHistoryProductViewModel: NonMemberOrderHistoryProductViewModel, position: Int) {
        Log.d("orderHistoryProductAdapter3", "order_history_product_list: ${purchaseList[position]}")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getTrackingInfo(
                        apiKey = "qo9jf3bIFZ4aEzIscfGPPQ",
                        courierCode = courierCode,
                        invoiceNumber = invoiceNumber
                    )
                }

                nonMemberOrderHistoryProductViewModel.shipping_status_list.value = response
                Log.d("trackingDetails33", "trackingDetails: ${nonMemberOrderHistoryProductViewModel.shipping_status_list.value}")
                Log.d("trackingDetails33", "------")

            } catch (e: HttpException) {
                Log.d("HTTP 오류 발생", "HTTP 오류 발생: ${e.code()} - ${e.message()}")
            } catch (e: Exception) {
                Log.d("기타 오류 발생", "기타 오류 발생: ${e.message}")
            }
        }
    }

    // 주문 상품 리스트 이미지 세팅
    fun setting_product_list_image(holder: RowNonMemberOrderHistoryProductBinding, position: Int) {
        // 상품 이미지 로드
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                ShoppingCartRepository.getting_image(purchaseList[position].images)
            }
            val uri_image = work1.await()

            holder.ivRowNonMemberOrderHistoryProductImage.loadImage(uri_image)
        }
    }

}