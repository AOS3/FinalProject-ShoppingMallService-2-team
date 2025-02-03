package com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.ui.adapter.ProductClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.ProductGridAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.MyBookmarkViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentMyBookmarkBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.getUserType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyBookmarkFragment: Fragment(), ProductClickListener {
    private var _binding: FragmentMyBookmarkBinding? = null
    private val binding get() = _binding!!

    private val bookmarkAdapter: ProductGridAdapter by lazy { ProductGridAdapter(this) }

    private val viewModel: MyBookmarkViewModel by viewModels()

    private lateinit var appContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadProductList(appContext.getUserId())
        setupUI()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onProductClick(product: Product) {
        val action = MyBookmarkFragmentDirections.actionNavigationMyBookmarkToNavigationProductDetail(product.productId)
        findNavController().navigate(action)
    }

    override fun onFavoriteClick(product: Product) {
        viewModel.toggleFavorite(appContext.getUserId(), product.productId)
    }

    private fun setupUI() {
        with(binding) {
            rvMyBookmarkList.adapter = bookmarkAdapter
        }
    }

    private fun setupListeners() {
        with(binding) {
            toolbarMyBookmark.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            toolbarMyBookmark.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_cart -> {
                        val action =
                            MyBookmarkFragmentDirections.actionNavigationMyBookmarkToNavigationCart()
                        findNavController().navigate(action)
                        true
                    }

                    else -> false
                }
            }
        }
    }


    private fun observeViewModel() {
        viewModel.productList.observe(viewLifecycleOwner) {
            bookmarkAdapter.submitList(it)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.shimmerMyBookmark.root.visibility = if (it) View.VISIBLE else View.GONE
            binding.rvMyBookmarkList.visibility = if (it) View.GONE else View.VISIBLE
        }
    }

}