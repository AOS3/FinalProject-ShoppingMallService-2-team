package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.ShoppingCart
import com.nemodream.bangkkujaengi.customer.data.repository.ShoppingCartRepository
import com.nemodream.bangkkujaengi.customer.ui.fragment.ShoppingCartFragment
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.RowShoppingCartRecyclerviewViewModel
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ShoppingCartViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentShoppingCartBinding
import com.nemodream.bangkkujaengi.databinding.RowShoppingCartRecyclerviewBinding
import com.nemodream.bangkkujaengi.utils.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ShoppingCartAdapter(
    private val cart_user_id: String,
    private val cart_product_data_list: MutableList<Product>,
    private val cart_data_list: ShoppingCart,
    private val viewLifecycleOwner: LifecycleOwner,
    private val shoppingCartViewModel: ShoppingCartViewModel,
    private val fragmentShoppingCartBinding: FragmentShoppingCartBinding,
    private val shoppingCartFragment: ShoppingCartFragment,
    private val user_type: String,
    private val onRefresh: () -> Unit // RecyclerView refresh callback
) : RecyclerView.Adapter<ShoppingCartAdapter.ShoppingCartViewHolder>() {

    inner class ShoppingCartViewHolder(val rowShoppingCartRecyclerviewBinding: RowShoppingCartRecyclerviewBinding) :
        RecyclerView.ViewHolder(rowShoppingCartRecyclerviewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartViewHolder {
        val binding = RowShoppingCartRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShoppingCartViewHolder(binding)
    }

    override fun getItemCount(): Int = cart_product_data_list.size

    override fun onBindViewHolder(holder: ShoppingCartViewHolder, position: Int) {
        val rowShoppingCartViewModel = RowShoppingCartRecyclerviewViewModel()
        Log.d("test333", "setting_toolbar: ${cart_data_list.items[position].checked}")

        // 상품 이미지 로딩
        holder.rowShoppingCartRecyclerviewBinding.pbRowShoppingCartProductImageLoading.visibility = View.VISIBLE
        holder.rowShoppingCartRecyclerviewBinding.ivRowShoppingCartProductImage.visibility = View.GONE

        when(user_type) {
            "member" -> {

            }
            else -> {
                holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductSalePercent.visibility =
                    View.GONE
                holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductOriginPrice.visibility =
                    View.GONE
            }
        }

        // 상품 이미지 로드
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                ShoppingCartRepository.getting_image(cart_product_data_list[position].images[0])
            }
            val uri_image = work1.await()
            holder.rowShoppingCartRecyclerviewBinding.pbRowShoppingCartProductImageLoading.visibility = View.GONE
            holder.rowShoppingCartRecyclerviewBinding.ivRowShoppingCartProductImage.visibility = View.VISIBLE
            holder.rowShoppingCartRecyclerviewBinding.ivRowShoppingCartProductImage.loadImage(uri_image)
        }

        /////////////////////////////ViewModel 초기 값 설정////////////////////////////////////////
        // 상품 선택 여부 초기 데이터 저장
        rowShoppingCartViewModel.cb_row_shopping_cart_product_select_checked.value =
            cart_data_list.items[position].checked

        // 상품명 초기 데이터 저장
        rowShoppingCartViewModel.tv_row_shopping_cart_product_name.value =
            cart_product_data_list[position].productName

        // 할인 비율 초기 데이터 저장
        rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_percent.value = when(user_type){
            "member" -> {
                cart_product_data_list[position].saleRate
            }
            else -> {
                0
            }
        }

        // 할인 전 가격 초기 데이터 저장
        rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value = when(user_type){
            "member" -> {
                cart_product_data_list[position].price * cart_data_list.items[position].quantity
            }
            else -> {
                cart_product_data_list[position].price * cart_data_list.items[position].quantity
            }
        }


        // 할인 후 가격 초기 데이터 저장
        // 원가
        val originalPrice = cart_product_data_list[position].price
        // 할인율
        val discountRate = cart_product_data_list[position].saleRate
        rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_price.value = when(user_type){
            "member" -> {
                ((originalPrice * (1 - (discountRate / 100.0))).toInt() * cart_data_list.items[position].quantity)
            }
            else -> {
                cart_product_data_list[position].price * cart_data_list.items[position].quantity
            }
        }

        // 상품 갯수 초기 데이터 저장
        rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value =
            cart_data_list.items[position].quantity

        // 상품 이미지 초기 경로 저장
        rowShoppingCartViewModel.iv_row_shopping_cart_product_image.value =
            cart_product_data_list[position].images[0]

        //////////////////////////// 상품 가격 요약 텍스트 초기 데이터 //////////////////////////////
        // 총 상품 가격 초기 데이터 저장
        if (cart_data_list.items[position].checked) {
            shoppingCartViewModel.tv_shopping_cart_tot_price_text.value =
                shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!!.plus(
                    rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value!!
                )

            shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value = when(user_type) {
                "member" -> {
                    shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!!.plus(
                        rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value!! -
                                rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_price.value!!
                    )
                }
                else -> {
                    0
                }
            }

            // 총 합 금액 업데이트
            shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value = when(user_type) {
                "member" -> {
                    shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! -
                            shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!! +
                            shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                }
                else -> {
                    shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! +
                            shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////


        ////////////////////////////////////// UI 업데이트 ///////////////////////////////////////
        // 상품 선택 여부
        rowShoppingCartViewModel.cb_row_shopping_cart_product_select_checked.observe(viewLifecycleOwner) {
            holder.rowShoppingCartRecyclerviewBinding.cbRowShoppingCartProductSelect.isChecked = it
        }

        // 상품명 업데이트
        rowShoppingCartViewModel.tv_row_shopping_cart_product_name.observe(viewLifecycleOwner) {
            holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductName.text = it
        }

        if (cart_data_list.items[position].color == "") {
            holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductOption.text =
                "옵션 : 없음"
        }
        else {
            holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductOption.text =
                "옵션 : " + cart_data_list.items[position].color
        }
        
        // 할인 비율 업데이트
        rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_percent.observe(viewLifecycleOwner) {
            when(user_type) {
                "member" -> {
                    holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductSalePercent.text = it.toString() + "%"
                }
                else -> {
                    holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductSalePercent.text =
                        "%"
                }
            }
        }
        // 원가 (할인 전 가격) 업데이트
        rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.observe(viewLifecycleOwner) {
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
            holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductOriginPrice.apply {
                text = formattedPrice
                // Paint.STRIKE_THRU_TEXT_FLAG : 텍스트에 사선을 추가하는 플래그
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // 텍스트에 사선 추가
            }
        }
        // 판매가 (할인 후 가격) 업데이트
        rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_price.observe(viewLifecycleOwner) {
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
            holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductSalePrice.text = formattedPrice
        }
        // 수량 업데이트
        rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.observe(viewLifecycleOwner) {
            holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductCnt.text = it.toString()
        }
        ////////////////////////////////////////////////////////////////////////////////////////////


        /////////////////////////////// 버튼, 체크 박스 이벤트 처리  ///////////////////////////////

        // 체크박스 상태 변경 시 결제 가격 요약을 업데이트
        holder.rowShoppingCartRecyclerviewBinding.cbRowShoppingCartProductSelect.setOnCheckedChangeListener { _, isChecked ->
            rowShoppingCartViewModel.cb_row_shopping_cart_product_select_checked.value = isChecked

            // 상품의 CheckBox를 체크할 경우
            if (isChecked) {
                // 체크 카운터 +1
                shoppingCartViewModel.checked_cnt.value =
                    shoppingCartViewModel.checked_cnt.value!!.plus(1)
                CoroutineScope(Dispatchers.Main).launch {
                    val work1 = async(Dispatchers.IO) {
                        ShoppingCartRepository.update_cart_item_checked_by_user_id(
                            cart_user_id,
                            cart_data_list.items[position].productId,
                            isChecked
                        )
                    }.join()
                    // 총 상품 가격 업데이트
                    shoppingCartViewModel.tv_shopping_cart_tot_price_text.value =
                        shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!!.plus(
                            rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value!!
                        )

                    // 총 할인 가격 업데이트
                    shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value = when(user_type) {
                        "member" -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!!.plus(
                                rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value!! -
                                        rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_price.value!!
                            )
                        }
                        else -> {
                            0
                        }
                    }

                    // 총 합 금액 업데이트
                    shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value = when(user_type) {
                        "member" -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! -
                                    shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!! +
                                    shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                        }
                        else -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! +
                                    shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                        }
                    }
                }
            }

            // 상품의 CheckBox의 체크를 해제할 경우
            else {
                // 체크 카운터 -1
                shoppingCartViewModel.checked_cnt.value =
                    shoppingCartViewModel.checked_cnt.value!!.minus(1)
                CoroutineScope(Dispatchers.Main).launch {
                    val work1 = async(Dispatchers.IO) {
                        ShoppingCartRepository.update_cart_item_checked_by_user_id(
                            cart_user_id,
                            cart_data_list.items[position].productId,
                            isChecked
                        )
                    }.join()

                    // 총 상품 가격 업데이트
                    shoppingCartViewModel.tv_shopping_cart_tot_price_text.value =
                        shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!!.minus(
                            rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value!!
                        )

                    // 총 할인 가격 업데이트
                    shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value = when(user_type) {
                        "member" -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!!.minus(
                                rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value!! -
                                        rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_price.value!!
                            )
                        }
                        else -> {
                            0
                        }
                    }

                    shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value = when(user_type) {
                        "member" -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value!!.minus(
                                // 총 상품 가격
                                rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value!! +
                                        // 총 할인 가격
                                        shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!! +
                                        // 총 배송비
                                        shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                            )
                        }
                        else -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value!!.minus(
                                // 총 상품 가격
                                rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value!! +
                                        // 총 배송비
                                        shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                            )
                        }
                    }

                    // 총 합 금액 업데이트
                    shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value = when(user_type) {
                        "member" -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! -
                                    shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!! +
                                    shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                        }
                        else -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! +
                                    shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                        }
                    }

                }

            }

        }

        // 수량 증가 버튼 클릭 이벤트
        holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntAdd.setOnClickListener {
            // 버튼 클릭 시 ProgressBar를 표시하고 버튼 숨기기
            holder.rowShoppingCartRecyclerviewBinding.pbRowShoppingCartLoading.visibility = View.VISIBLE
            holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntAdd.visibility = View.GONE
            holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntRemove.visibility = View.GONE
            holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductCnt.visibility = View.GONE

            // 수량 1 증가
            rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value =
                rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value?.plus(1)

            Log.d("test00", "setting_toolbar: ${cart_product_data_list[position].productId}")
            // 수량 데이터 업데이트
            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO) {
                    ShoppingCartRepository.update_cart_item_quantity(
                        cart_user_id,
                        cart_data_list.items[position].productId,
                        rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!!
                    )
                }
                work1.await()

                // 할인 전 가격 값 업데이트
                rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value =
                    cart_product_data_list[position].price * rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!!


                // 할인 후 가격 값 업데이트
                // 원가
                val originalPrice = cart_product_data_list[position].price
                // 할인율
                val discountRate = cart_product_data_list[position].saleRate
                rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_price.value = when(user_type) {
                    "member" -> {
                        ((originalPrice * (1 - (discountRate / 100.0))).toInt() * rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!!)
                    }
                    else -> {
                        cart_product_data_list[position].price * rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!!
                    }
                }


                // checkbox 가 true일 경우 총 상품 가격 값을 업데이트 한다.
                if (rowShoppingCartViewModel.cb_row_shopping_cart_product_select_checked.value!!) {

                    // 총 상품 가격 값 업데이트
                    shoppingCartViewModel.tv_shopping_cart_tot_price_text.value =
                        shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!!.plus(
                            cart_product_data_list[position].price
                        )

                    // 총 할인 가격 값 업데이트
                    // 원가
                    val originalPrice = cart_product_data_list[position].price
                    // 할인율
                    val discountRate = cart_product_data_list[position].saleRate
                    shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value = when(user_type) {
                        "member" -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!!.plus(
                                cart_product_data_list[position].price -
                                        ((originalPrice * (1 - (discountRate / 100.0))).toInt())
                            )
                        }
                        else -> {
                            0
                        }
                    }

                    // 총 합 금액 업데이트
                    shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value = when(user_type) {
                        "member" -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! -
                                    shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!! +
                                    shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                        }
                        else -> {
                            shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! +
                                    shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                        }
                    }

                }

                // 작업 완료 후 ProgressBar 숨기고 버튼 복원
                holder.rowShoppingCartRecyclerviewBinding.pbRowShoppingCartLoading.visibility = View.GONE
                holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntAdd.visibility = View.VISIBLE
                holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntRemove.visibility = View.VISIBLE
                holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductCnt.visibility = View.VISIBLE
            }
        }

        // 수량 감소 버튼 클릭 이벤트
        holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntRemove.setOnClickListener {
            if (rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!! > 1) {
                // 버튼 클릭 시 ProgressBar를 표시하고 버튼 숨기기
                holder.rowShoppingCartRecyclerviewBinding.pbRowShoppingCartLoading.visibility = View.VISIBLE
                holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntAdd.visibility = View.GONE
                holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntRemove.visibility = View.GONE
                holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductCnt.visibility = View.GONE

                // 수량 1 감소
                rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value =
                    rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value?.minus(1)

                // 수량 데이터 업데이트
                viewLifecycleOwner.lifecycleScope.launch {
                    val work1 = async(Dispatchers.IO) {
                        ShoppingCartRepository.update_cart_item_quantity(
                            cart_user_id,
                            cart_data_list.items[position].productId,
                            rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!!
                        )
                    }
                    work1.join()

                    // 할인 전 가격 값 업데이트
                    rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value =
                        cart_product_data_list[position].price * rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!!

                    // 할인 후 가격 값 업데이트
                    // 원가
                    val originalPrice = cart_product_data_list[position].price
                    // 할인율
                    val discountRate = cart_product_data_list[position].saleRate
                    rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_price.value = when(user_type) {
                        "member" -> {
                            ((originalPrice * (1 - (discountRate / 100.0))).toInt() * rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!!)
                        }
                        else -> {
                            cart_product_data_list[position].price * rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value!!
                        }
                    }

                    // checkbox 가 true일 경우 총 상품 가격 값을 업데이트 한다.
                    if (rowShoppingCartViewModel.cb_row_shopping_cart_product_select_checked.value!!) {
                        // 총 상품 가격 값 업데이트
                        shoppingCartViewModel.tv_shopping_cart_tot_price_text.value =
                            shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!!.minus(
                                cart_product_data_list[position].price
                            )

                        // 총 할인 가격 값 업데이트
                        // 원가
                        val originalPrice = cart_product_data_list[position].price
                        // 할인율
                        val discountRate = cart_product_data_list[position].saleRate
                        shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value = when(user_type) {
                            "member" -> {
                                shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!!.minus(
                                    cart_product_data_list[position].price -
                                            ((originalPrice * (1 - (discountRate / 100.0))).toInt())
                                )
                            }
                            else -> {
                                0
                            }
                        }

                        // 총 합 금액 업데이트
                        shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value = when(user_type) {
                            "member" -> {
                                shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! -
                                        shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value!! +
                                        shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                            }
                            else -> {
                                shoppingCartViewModel.tv_shopping_cart_tot_price_text.value!! +
                                        shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value!!
                            }
                        }

                    }

                    // 작업 완료 후 ProgressBar 숨기고 버튼 복원
                    holder.rowShoppingCartRecyclerviewBinding.pbRowShoppingCartLoading.visibility = View.GONE
                    holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntAdd.visibility = View.VISIBLE
                    holder.rowShoppingCartRecyclerviewBinding.ibtnRowShoppingCartProductCntRemove.visibility = View.VISIBLE
                    holder.rowShoppingCartRecyclerviewBinding.tvRowShoppingCartProductCnt.visibility = View.VISIBLE
                }

            }
        }

        // 삭제 버튼 클릭
        holder.rowShoppingCartRecyclerviewBinding.btnRowShoppingCartProductDelete.setOnClickListener {
            // 로딩바 활성화
            fragmentShoppingCartBinding.pdShoppingCartProductListLoading.visibility = View.VISIBLE
            // recyclerview 비활성화
            fragmentShoppingCartBinding.rvShoppingCartProductList.visibility = View.GONE

            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO) {
                    ShoppingCartRepository.delete_cart_item_by_product_id(
                        cart_user_id,
                        cart_data_list.items[position].productId
                    )
                }
                work1.await()

                shoppingCartFragment.refresh_recyclerview_shopping_cart()
            }
        }

        // 선택 삭제 버튼 클릭
        fragmentShoppingCartBinding.btnShoppingCartSelectProductDelete.setOnClickListener {
            // 로딩바 활성화
            fragmentShoppingCartBinding.pdShoppingCartProductListLoading.visibility = View.VISIBLE
            // recyclerview 비활성화
            fragmentShoppingCartBinding.rvShoppingCartProductList.visibility = View.GONE

            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO) {
                    ShoppingCartRepository.delete_cart_item_by_checked(
                        cart_user_id,
                    )
                }
                work1.await()

                rowShoppingCartViewModel.cb_row_shopping_cart_product_select_checked.value = false

                rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_percent.value = 0
                // 원가 (할인 전 가격)
                rowShoppingCartViewModel.tv_row_shopping_cart_product_origin_price.value = 0
                // 판매가 (할인 후 가격)
                rowShoppingCartViewModel.tv_row_shopping_cart_product_sale_price.value = 0
                // 수량
                rowShoppingCartViewModel.tv_row_shopping_cart_product_cnt.value = 0

                shoppingCartFragment.refresh_recyclerview_shopping_cart()
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////


    }
}
