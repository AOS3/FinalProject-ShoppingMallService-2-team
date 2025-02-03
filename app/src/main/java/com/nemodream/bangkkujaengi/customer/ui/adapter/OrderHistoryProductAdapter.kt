package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nemodream.bangkkujaengi.customer.data.model.PurchaseState
import com.nemodream.bangkkujaengi.customer.data.repository.OrderHistoryRepository
import com.nemodream.bangkkujaengi.customer.data.repository.ShoppingCartRepository
import com.nemodream.bangkkujaengi.customer.deliverytracking.RetrofitInstance
import com.nemodream.bangkkujaengi.customer.ui.fragment.OrderHistoryFragment
import com.nemodream.bangkkujaengi.customer.ui.fragment.OrderHistoryFragmentDirections
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.OrderHistoryProductAdapterViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.OrderHistoryProductViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.OrderHistoryViewModel
import com.nemodream.bangkkujaengi.databinding.OrderHistoryProductCustomDialogBinding
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

class OrderHistoryProductAdapter(
    val orderHistoryFragment : OrderHistoryFragment,
    val orderHistoryViewModel : OrderHistoryViewModel,
    val orderHistoryProductViewModel : OrderHistoryProductViewModel,
    val viewLifecycleOwner : LifecycleOwner
) : RecyclerView.Adapter<OrderHistoryProductAdapter.OrderHistoryProductViewHolder>() {

    inner class OrderHistoryProductViewHolder(val rowOrderHistoryProductBinding: RowOrderHistoryProductBinding) :
        RecyclerView.ViewHolder(rowOrderHistoryProductBinding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderHistoryProductViewHolder {
        val rowOrderHistoryProductBinding = RowOrderHistoryProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return OrderHistoryProductViewHolder(rowOrderHistoryProductBinding)
    }

    override fun getItemCount(): Int = orderHistoryProductViewModel.order_history_product_list.value!!.size

    override fun onBindViewHolder(holder: OrderHistoryProductViewHolder, position: Int) {

        val orderHistoryProductAdapterViewModel = OrderHistoryProductAdapterViewModel()


        // 배송 상세 내용 저장
        fetchTrackingInfo(
            "04",
            orderHistoryProductViewModel.order_history_product_list.value!![position].purchaseInvoiceNumber.toString(),
            orderHistoryProductAdapterViewModel,
            position
        )

        setting_btn_purchase_confirmation(
            holder.rowOrderHistoryProductBinding,
            orderHistoryProductAdapterViewModel,
            position
        )

        // 주문 상품 리스트 이미지 세팅
        setting_product_list_image(holder.rowOrderHistoryProductBinding, position)

        // 주문 상품 리스트 텍스트 세팅
        setting_product_list_text(holder.rowOrderHistoryProductBinding, orderHistoryProductAdapterViewModel, position)

        // 배송 현황 버튼
        setting_btn_row_order_history_product_shipping_status_check(holder.rowOrderHistoryProductBinding, orderHistoryProductAdapterViewModel, position)

    }

    fun setting_btn_purchase_confirmation(
        rowOrderHistoryProductBinding : RowOrderHistoryProductBinding,
        orderHistoryProductAdapterViewModel : OrderHistoryProductAdapterViewModel,
        position: Int
    ) {
        rowOrderHistoryProductBinding.btnRowOrderHistoryProductPurchaseConfirmation.setOnClickListener {
            // 다이얼로그 뷰 바인딩
            val inflater = LayoutInflater.from(rowOrderHistoryProductBinding.root.context)
            val customDialogBinding = OrderHistoryProductCustomDialogBinding.inflate(inflater)

            val builder = MaterialAlertDialogBuilder(rowOrderHistoryProductBinding.root.context)
                .setView(customDialogBinding.root)
                .setCancelable(true)

            val dialog = builder.create()

            customDialogBinding.apply {
                // 확인 버튼 이벤트
                btnOrderHistoryProductCheck.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val work1 = async(Dispatchers.IO) {
                            OrderHistoryRepository.update_purchase_state(
                                orderHistoryProductViewModel.order_history_product_list.value!![position].documentId
                            )
                        }.await()
                        rowOrderHistoryProductBinding.tvRowOrderHistoryProductDeliveryState.text = "구매 확정"
                        rowOrderHistoryProductBinding.btnRowOrderHistoryProductPurchaseConfirmation.visibility = View.GONE
                        Log.d("asdfasdf", orderHistoryProductViewModel.order_history_product_list.value!![position].documentId)
                        dialog.dismiss()
                    }
                }

                // 취소 버튼 이벤트
                btnOrderHistoryProductCancel.setOnClickListener {
                    // 로그인 처리 로직 추가
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }

    // 배송 현황 버튼
    fun setting_btn_row_order_history_product_shipping_status_check(
        rowOrderHistoryProductBinding : RowOrderHistoryProductBinding,
        orderHistoryProductAdapterViewModel : OrderHistoryProductAdapterViewModel,
        position: Int
    ) {
        rowOrderHistoryProductBinding.btnRowOrderHistoryProductShippingStatusCheck.setOnClickListener {
            val action = OrderHistoryFragmentDirections.actionOrderHistoryFragmentToShippingStatusFragment(
                orderHistoryProductViewModel.order_history_product_list.value!![position].documentId,
                orderHistoryProductViewModel.order_history_product_list.value!![position].purchaseDateTime,
                orderHistoryProductAdapterViewModel.shipping_status_list.value!!
            )
            orderHistoryFragment.findNavController().navigate(action)
        }

    }

    // 주문 상품 리스트 텍스트 세팅
    fun setting_product_list_text(holder: RowOrderHistoryProductBinding, orderHistoryProductAdapterViewModel: OrderHistoryProductAdapterViewModel, position: Int) {

        // 주문 상품 이름
        holder.tvRowOrderHistoryProductProductName.text =
            orderHistoryProductViewModel.order_history_product_list.value!![position].productTitle

        // 색깔 옵션
        if (orderHistoryProductViewModel.order_history_product_list.value!![position].color == "") {
            holder.tvRowOrderHistoryProductProductOption.visibility = View.GONE
        } else {
            holder.tvRowOrderHistoryProductProductOption.text =
                "옵션 : " + orderHistoryProductViewModel.order_history_product_list.value!![position].color
        }

        // 주문 상품 가격
        val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(
            orderHistoryProductViewModel.order_history_product_list.value!![position].totPrice
        ) + " 원"
        holder.tvRowOrderHistoryProductPrice.text = formattedPrice

        // 주문 상품 수량
        val formattedCnt = NumberFormat.getNumberInstance(Locale.KOREA).format(
            orderHistoryProductViewModel.order_history_product_list.value!![position].purchaseQuantity
        ) + "개"
        holder.tvRowOrderHistoryProductCnt.text = formattedCnt

        orderHistoryProductAdapterViewModel.shipping_status_list.observe(viewLifecycleOwner) {
            Log.d("trackingDetails3", "${it.level}")
            if (orderHistoryProductViewModel.order_history_product_list.value!![position].purchaseState != PurchaseState.PURCHASE_CONFIRMED.name) {
                holder.tvRowOrderHistoryProductDeliveryState.text = when(it.level) {
                    1 -> "배송 준비 중"
                    2,3,4,5 -> "배송 중"
                    6 -> "배송 완료"
                    else -> "결제 완료"
                }
                if (it.level == 6) {
                    holder.btnRowOrderHistoryProductPurchaseConfirmation.visibility = View.VISIBLE
                } else {
                    holder.btnRowOrderHistoryProductPurchaseConfirmation.visibility = View.GONE
                }
            }
            else {
                holder.tvRowOrderHistoryProductDeliveryState.text = "구매 확정"
            }
        }
    }

    // api를 통해 배송 상세 내용을 저장
    fun fetchTrackingInfo(courierCode: String, invoiceNumber: String, orderHistoryProductAdapterViewModel: OrderHistoryProductAdapterViewModel, position: Int) {
        Log.d("orderHistoryProductAdapter3", "order_history_product_list: ${orderHistoryProductViewModel.order_history_product_list.value!![position]}")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getTrackingInfo(
                        apiKey = "qo9jf3bIFZ4aEzIscfGPPQ",
                        courierCode = courierCode,
                        invoiceNumber = invoiceNumber
                    )
                }

                orderHistoryProductAdapterViewModel.shipping_status_list.value = response
                Log.d("trackingDetails2", "${orderHistoryProductViewModel.tracking_response.value}")
                Log.d("trackingDetails2", "------")

            } catch (e: HttpException) {
                Log.d("HTTP 오류 발생", "HTTP 오류 발생: ${e.code()} - ${e.message()}")
            } catch (e: Exception) {
                Log.d("기타 오류 발생", "기타 오류 발생: ${e.message}")
            }
        }
    }

    // 주문 상품 리스트 이미지 세팅
    fun setting_product_list_image(holder: RowOrderHistoryProductBinding, position: Int) {
        // 상품 이미지 로드
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                ShoppingCartRepository.getting_image(orderHistoryProductViewModel.order_history_product_list.value!![position].images)
            }
            val uri_image = work1.await()

            holder.ivRowOrderHistoryProductImage.loadImage(uri_image)
        }
    }

}