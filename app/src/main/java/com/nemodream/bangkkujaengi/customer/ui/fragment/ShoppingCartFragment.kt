package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.customer.data.model.PaymentItems
import com.nemodream.bangkkujaengi.customer.data.model.PaymentProduct
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.ShoppingCart
import com.nemodream.bangkkujaengi.customer.data.repository.ShoppingCartRepository
import com.nemodream.bangkkujaengi.customer.ui.adapter.ShoppingCartAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.ShoppingCartViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentShoppingCartBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.getUserType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ShoppingCartFragment : Fragment() {

    lateinit var fragmentShoppingCartBinding: FragmentShoppingCartBinding
    val shoppingCartViewModel : ShoppingCartViewModel by viewModels()

    var productList = Array(3) {
        "상품 테스트 ${it}"
    }

    // var cart_document_id_list = mutableListOf<String>()
    // 회원 or 비회원
    var user_type = ""
    var user_id = ""

    // 장바구니 document id를 변수
    var cart_user_id:String = ""
    // 장바구니 아이템 정보를 담을 리스트
    // var cart_data_list = mutableListOf<ShoppingCart>()
    var cart_data_list = ShoppingCart()

    // 장바구니에 담긴 상품 정보를 담을 리스트
    var cart_product_data_list = mutableListOf<Product>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentShoppingCartBinding  = FragmentShoppingCartBinding.inflate(inflater, container, false)

        // 유저 id 세팅 메소드 호출
        setting_user_id()
        // 툴바 세팅 메소드 호출
        setting_toolbar()
        // 초기 총 상품 가격 요약 텍스트 세팅 메소드 호출
        setting_price_textview()
        // 버튼 기능 메소드 호출
        fn_btn_shopping_cart_buy()
        // recycelrview 초기 세팅 메소드 호출
        setting_recyclerview()


        // fn_test_button()
        // fn_btn_shopping_cart_order_history()
        // fn_btn_non_member_order_fragment()

        return fragmentShoppingCartBinding.root
    }

    // 유저 id 세팅 메소드
    fun setting_user_id() {
        // Log.d("test1213", "setting_user_id: ${requireContext().getUserId()}")
        user_type = requireContext().getUserType()
        when(user_type) {
            "member" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val work1 = async(Dispatchers.IO) {
                        ShoppingCartRepository.getting_user_id_by_document_id(requireContext().getUserId())
                    }
                    user_id = work1.await()
                    Log.d("test1213", "setting_user_id: ${user_id}")
                    // recycelrview 업데이트 메소드 호출
                    refresh_recyclerview_shopping_cart()
                }
            }
            "guest" -> {
                user_id = requireContext().getUserId()
                Log.d("test1213", "setting_user_id: ${user_id}")
                // recycelrview 업데이트 메소드 호출
                refresh_recyclerview_shopping_cart()
            }
            else -> {
                ""
            }
        }
    }

    // 툴바 세팅 메소드
    fun setting_toolbar() {
        fragmentShoppingCartBinding.tbShoppingCart.apply {
            // 툴바에 뒤로가기 버튼 아이콘 생성
            setNavigationIcon(R.drawable.ic_arrow_back)
            // 툴바 뒤로가기 버튼의 이벤트
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    // 초기 총 상품 가격 요약 텍스트 세팅 메소드
    fun setting_price_textview() {

        //////////////////////////////// 상품 가격 요약 업데이트 //////////////////////////////////////
        // 총 상품 가격 업데이트
        shoppingCartViewModel.tv_shopping_cart_tot_price_text.observe(viewLifecycleOwner){
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
            fragmentShoppingCartBinding.tvShoppingCartTotPrice.text = formattedPrice
        }

        // 총 할인 가격 업데이트
        shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.observe(viewLifecycleOwner){
            val formattedPrice = "- " + NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
            fragmentShoppingCartBinding.tvShoppingCartTotSalePrice.text = formattedPrice
        }

        // 총 배송비 가격 업데이트
        shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.observe(viewLifecycleOwner){
            val formattedPrice = "+ " + NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
            fragmentShoppingCartBinding.tvShoppingCartTotDeliveryCost.text = formattedPrice
        }

        // 총 합 금액  업데이트
        shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.observe(viewLifecycleOwner){
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(it) + " 원"
            fragmentShoppingCartBinding.tvShoppingCartTotSumPrice.text = formattedPrice
        }

        shoppingCartViewModel.checked_cnt.observe(viewLifecycleOwner) {
            shoppingCartViewModel.btn_shopping_cart_buy_text.value = "구매하기 (${it})"
            Log.d("check_cnt", "${it}")

            if (it == 0) {
                fragmentShoppingCartBinding.btnShoppingCartBuy.apply {
                    isEnabled = false
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#52332828"))
                    setTextColor(Color.parseColor("#ffffff"))
                    // backgroundTintList = ColorStateList.valueOf(Color.parseColor("#52332828"))
                }
            }
            else {
                fragmentShoppingCartBinding.btnShoppingCartBuy.apply {
                    isEnabled = true
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#332828"))
                    setTextColor(Color.parseColor("#ffffff"))
                    // backgroundTintList = ColorStateList.valueOf(Color.parseColor("#52332828"))
                }
            }

        }
        ////////////////////////////////////////////////////////////////////////////////////////////

    }

    // 버튼 기능 메소드 - 구매 하기
    fun fn_btn_shopping_cart_buy() {

        // 구매하기 버튼 텍스트 옵저버
        shoppingCartViewModel.btn_shopping_cart_buy_text.observe(viewLifecycleOwner){
            fragmentShoppingCartBinding.btnShoppingCartBuy.text = it
        }

        fragmentShoppingCartBinding.btnShoppingCartBuy.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val work1 = async(Dispatchers.IO) {
                    ShoppingCartRepository.getting_user_data_by_user_id(user_id)
                }
                val user_data = work1.await()

                expectation_price_rest_to_0()

                val memberData = user_data?.get("member_data") as? Member

                Log.d("user_data", "${memberData?.memberPhoneNumber}")

                val action = ShoppingCartFragmentDirections.actionNavigationCartToPaymentFragment(
                    user_id,
                    user_type,
                    memberData?.memberName.toString(),
                    memberData?.memberPhoneNumber.toString(),
                    "서울시 강남구 역삼동",
                    "cart",
                    PaymentProduct()
                )
                findNavController().navigate(action)
            }
        }
    }

    // 임시 버튼 /////////////////////////////////////////////////////////////////////////////////////

    // 데이터 추가 테스트 버튼
    fun fn_test_button() {
        fragmentShoppingCartBinding.btnShoppingCartItemAdd.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO) {
//                    ShoppingCartRepository.add_cart_items_by_product_ids(user_id)
                }.await()
            }
        }
    }

    // 주문 내역 가는 테스트 버튼
    fun fn_btn_shopping_cart_order_history() {
        fragmentShoppingCartBinding.btnShoppingCartOrderHistory.setOnClickListener {
            val action = ShoppingCartFragmentDirections.actionNavigationCartToOrderHistoryFragment()
            findNavController().navigate(action)
        }
    }

    fun fn_btn_non_member_order_fragment() {
        fragmentShoppingCartBinding.btnShoppingCartNonMemberOrderHistory.setOnClickListener {
            val action = ShoppingCartFragmentDirections.actionNavigationCartToNonMemberOrderFragment()
            findNavController().navigate(action)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // recycelrview 업데이트 메소드
    fun refresh_recyclerview_shopping_cart() {

        // 로딩바 활성화
        fragmentShoppingCartBinding.pdShoppingCartProductListLoading.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {

            // 장바구니 데이터를 가져온다
            val work1 = async(Dispatchers.IO) {
                // 테스트용 아이디를 임의로 넣었음
                ShoppingCartRepository.getting_shopping_cart_item_by_userId(user_id)
            }.await()

            cart_user_id = ""
            cart_data_list = ShoppingCart()

            cart_product_data_list = mutableListOf<Product>()

            if (work1.size == 0) {
                fragmentShoppingCartBinding.pdShoppingCartProductListLoading.visibility = View.GONE
                fragmentShoppingCartBinding.tvShoppingCartEmptyText.visibility = View.VISIBLE
                fragmentShoppingCartBinding.btnShoppingCartSelectProductDelete.visibility = View.GONE
                fragmentShoppingCartBinding.tvShoppingCartEmptyText.setText("장바구니가\n비어있습니다")
                return@launch
            }
            work1.forEach {
                val cart_data = it["cart_data"] as ShoppingCart
                // 장바구니 document id를 가져온다
                cart_user_id = cart_data.userId

                // 장바구니 데이터를 가져온다
                work1.map {
                    cart_data.items.forEach {
                        cart_data_list = cart_data
                    }
                }
            }

            // 먼저 체크된 상품 개수를 0으로 초기화한 후에 카운트 시작
            shoppingCartViewModel.checked_cnt.value = 0

            cart_data_list.items.forEach {
                if (it.checked == true) {
                    shoppingCartViewModel.checked_cnt.value =
                        shoppingCartViewModel.checked_cnt.value!!.plus(1)
                }
            }

            Log.d("asd", "${cart_data_list}")

            // 장바구니 데이터에서 가져온 상품 document_id를 통해 상품정보를 가져온다
            val work2 = async(Dispatchers.IO) {
                // Log.d("test300", "setting_toolbar: ${cart_data_list[0].items.forEach { it.productId }}")
                ShoppingCartRepository.getting_prodcut_by_product_document_id(cart_data_list.items.map { it.productId })
            }.await()
            work2.forEach {
                cart_product_data_list.add(it["product_data"] as Product)
            }

            cart_product_data_list.forEach {
                Log.d("test300", "setting_toolbar: ${it.productName}")
                Log.d("test300", "setting_toolbar: ${it.images}")
            }

            expectation_price_rest_to_0()

            // recycelrview 초기 세팅 메소드 호출
            setting_recyclerview()

            // 버튼 텍스트 업데이트
            shoppingCartViewModel.btn_shopping_cart_buy_text.value = "구매하기 (${shoppingCartViewModel.checked_cnt.value})"

            // 로딩바 비활성화
            fragmentShoppingCartBinding.pdShoppingCartProductListLoading.visibility = View.GONE
            // recyclerview 활성화
            fragmentShoppingCartBinding.rvShoppingCartProductList.visibility = View.VISIBLE

            fragmentShoppingCartBinding.rvShoppingCartProductList.adapter?.notifyDataSetChanged()
        }
    }

    // recycelrview 초기 세팅 메소드
    fun setting_recyclerview() {
        fragmentShoppingCartBinding.rvShoppingCartProductList.apply {
            adapter = ShoppingCartAdapter(
                cart_user_id,
                cart_product_data_list,
                cart_data_list,
                viewLifecycleOwner,
                shoppingCartViewModel,
                fragmentShoppingCartBinding,
                this@ShoppingCartFragment,
                user_type
            ) { refresh_recyclerview_shopping_cart() } // Refresh callback
            layoutManager = LinearLayoutManager(context)
        }
    }

    // 총 상품 가격 요약 텍스트 0으로 초기화 하는 메소드
    fun expectation_price_rest_to_0() {
        shoppingCartViewModel.tv_shopping_cart_tot_price_text.value = 0
        shoppingCartViewModel.tv_shopping_cart_tot_sale_price_text.value = 0
        // shoppingCartViewModel.tv_shopping_cart_tot_delivery_cost_text.value = 0
        shoppingCartViewModel.tv_shopping_cart_tot_sum_price_text.value = 0
    }

}