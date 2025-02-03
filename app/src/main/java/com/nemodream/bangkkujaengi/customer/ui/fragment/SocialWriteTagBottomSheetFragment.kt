package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SearchHistory
import com.nemodream.bangkkujaengi.customer.ui.adapter.OnItemClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.ProductClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.SearchResultAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialWriteTagBottomSheetViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentSocialWriteTagBottomSheetBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.hideKeyboard
import com.nemodream.bangkkujaengi.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocialWriteTagBottomSheetFragment : BottomSheetDialogFragment(), OnItemClickListener, ProductClickListener {

    private var _binding: FragmentSocialWriteTagBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var photoPosition: Int = -1
    private var xCoord: Float = 0f
    private var yCoord: Float = 0f

    private lateinit var appContext: Context
    private val viewModel: SocialWriteTagBottomSheetViewModel by viewModels()
    private val searchResultAdapter: SearchResultAdapter by lazy { SearchResultAdapter(this) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSocialWriteTagBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        setupListeners()

        arguments?.let {
            photoPosition = it.getInt("photoPosition", -1)
            xCoord = it.getFloat("xCoord", 0f)
            yCoord = it.getFloat("yCoord", 0f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        binding.rvSearchResults.adapter = searchResultAdapter
    }

    private fun observeViewModel() {
        with(binding) {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    shimmerLayout.root.visibility = View.VISIBLE
                    rvSearchResults.visibility = View.GONE
                    layoutResultEmpty.root.visibility = View.GONE
                } else {
                    shimmerLayout.root.visibility = View.GONE
                    if (viewModel.searchResults.value?.isNotEmpty() == true) {
                        rvSearchResults.visibility = View.VISIBLE
                        searchResultAdapter.submitList(viewModel.searchResults.value)
                    }
                }
            }

            viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->

                if (searchResults.isEmpty()) {
                    layoutResultEmpty.root.visibility = View.VISIBLE
                    rvSearchResults.visibility = View.GONE
                    tvSearchResultLabel.visibility = View.GONE
                } else {
                    layoutResultEmpty.root.visibility = View.GONE
                    rvSearchResults.visibility = View.VISIBLE
                    tvSearchResultLabel.apply {
                        text = setupSearchResultText(
                            etSearchTrack.editText?.text.toString(),
                            searchResults.size
                        )
                        visibility = View.VISIBLE
                    }
                }
                searchResultAdapter.submitList(searchResults)
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnSearchTrack.setOnClickListener {
                layoutEnterTagProduct.root.visibility = View.GONE
                tvSearchResultLabel.visibility = View.GONE
                performSearch()
                etSearchTrack.clearFocus()
                it.hideKeyboard()
            }

            etSearchTrack.editText?.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_SEARCH -> {
                        performSearch()
                        view?.hideKeyboard()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun performSearch() {
        val query = binding.etSearchTrack.editText?.text.toString().trim()
        if (query.isEmpty()) {
            appContext.showToast("검색어를 입력해주세요.")
            return
        }
        viewModel.getProductsByProductName(binding.etSearchTrack.editText?.text.toString(), appContext.getUserId())
    }

    private fun setupSearchResultText(query: String, resultCount: Int): SpannableStringBuilder {
        return SpannableStringBuilder().apply {
            append(query, StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("에 대한 검색 결과 ")
            append(resultCount.toString(), StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("건")
        }
    }

    override fun onItemClick(item: SearchHistory) {}

    override fun onDeleteClick(search: SearchHistory) {}

    override fun onProductClick(product: Product) {
        val resultData = Bundle().apply {
            putParcelable("selectedProduct", product)
            putInt("photoPosition", photoPosition)
            putFloat("xCoord", xCoord)
            putFloat("yCoord", yCoord)
        }
        parentFragmentManager.setFragmentResult("productWithTagData", resultData)
        dismiss()
    }

    override fun onFavoriteClick(product: Product) {}
}
