package com.nemodream.bangkkujaengi.admin.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.admin.ui.adapter.AdminProductAdapter
import com.nemodream.bangkkujaengi.admin.ui.adapter.OnProductClickListener
import com.nemodream.bangkkujaengi.admin.ui.viewmodel.AdminProductViewModel
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.databinding.FragmentAdminProductBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminProductFragment : Fragment(), OnProductClickListener {
    private var _binding: FragmentAdminProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminProductViewModel by viewModels()
    private val productAdapter by lazy { AdminProductAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        viewModel.loadProducts()
        with(binding) {
            rvAdminProductList.adapter = productAdapter
        }
    }

    private fun setupListeners() {
        with(binding) {
            toolbarProductProduct.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            btnProductAdd.setOnClickListener {
                val action = AdminProductFragmentDirections.actionAdminProductFragmentToAdminAddProductFragment()
                findNavController().navigate(action)
            }

        }

    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.adminProductProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvAdminProductList.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    override fun onProductClick(product: Product, view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.admin_product_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                val action =
                    AdminProductFragmentDirections.actionAdminProductFragmentToAdminEditProductFragment(
                        product
                    )
                findNavController().navigate(action)
                    true
                }
                R.id.menu_delete -> {
                    viewModel.deleteProduct(product.productId)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

}