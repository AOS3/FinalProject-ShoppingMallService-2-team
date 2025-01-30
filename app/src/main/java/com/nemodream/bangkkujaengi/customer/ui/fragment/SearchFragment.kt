package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nemodream.bangkkujaengi.customer.data.model.Product
import com.nemodream.bangkkujaengi.customer.data.model.SearchHistory
import com.nemodream.bangkkujaengi.customer.ui.adapter.OnItemClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.ProductClickListener
import com.nemodream.bangkkujaengi.customer.ui.adapter.SearchHistoryAdapter
import com.nemodream.bangkkujaengi.customer.ui.adapter.SearchResultAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SearchViewModel
import com.nemodream.bangkkujaengi.databinding.FragmentSearchBinding
import com.nemodream.bangkkujaengi.utils.getUserId
import com.nemodream.bangkkujaengi.utils.getUserType
import com.nemodream.bangkkujaengi.utils.hideKeyboard
import com.nemodream.bangkkujaengi.utils.showLoginSnackbar
import com.nemodream.bangkkujaengi.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(), OnItemClickListener, ProductClickListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var appContext: Context
    private val viewModel: SearchViewModel by viewModels()
    private val searchAdapter: SearchHistoryAdapter by lazy { SearchHistoryAdapter(this) }
    private val searchResultAdapter: SearchResultAdapter by lazy { SearchResultAdapter(this) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.rvSearchHistoryList.adapter = searchAdapter
        binding.rvSearchResultList.adapter = searchResultAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        with(binding) {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    shimmerLayout.root.visibility = View.VISIBLE
                    rvSearchResultList.visibility = View.GONE
                    rvSearchHistoryList.visibility = View.GONE
                    groupRecentSearches.visibility = View.GONE
                    layoutResultEmpty.root.visibility = View.GONE
                } else {
                    shimmerLayout.root.visibility = View.GONE
                    if (viewModel.searchResults.value?.isNotEmpty() == true) {
                        rvSearchResultList.visibility = View.VISIBLE
                        searchResultAdapter.submitList(viewModel.searchResults.value)
                    } else {
                        rvSearchHistoryList.visibility = View.GONE
                    }
                }
            }

            viewModel.searchHistory.observe(viewLifecycleOwner) { searchHistory ->
                if (viewModel.isLoading.value == true) return@observe

                if (searchHistory.isEmpty()) {
                    tvSearchHistoryLabel.visibility = View.GONE
                    tvClearAll.visibility = View.GONE
                    groupRecentSearches.visibility = View.GONE
                    layoutHistoryEmpty.root.visibility = View.VISIBLE
                } else {
                    groupRecentSearches.visibility = View.VISIBLE
                    layoutHistoryEmpty.root.visibility = View.GONE
                    searchAdapter.submitList(searchHistory)
                }
            }

            viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
                rvSearchHistoryList.visibility = View.GONE
                groupRecentSearches.visibility = View.GONE

                if (searchResults.isEmpty()) {
                    layoutResultEmpty.root.visibility = View.VISIBLE
                    rvSearchHistoryList.visibility = View.GONE
                    rvSearchResultList.visibility = View.GONE
                    tvSearchResultLabel.visibility = View.GONE
                } else {
                    layoutResultEmpty.root.visibility = View.GONE
                    rvSearchResultList.visibility = View.VISIBLE
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
            btnArrowBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnSearchTrack.setOnClickListener {
                performSearch()
                etSearchTrack.clearFocus()
                it.hideKeyboard()
            }

            tvClearAll.setOnClickListener {
                viewModel.clearAllSearches()
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
        viewModel.addSearch(query)
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

    override fun onItemClick(item: SearchHistory) {
        val query = item.query
        binding.etSearchTrack.editText?.setText(query)
        binding.etSearchTrack.editText?.setSelection(query.length)
        performSearch()
    }

    override fun onDeleteClick(search: SearchHistory) {
        viewModel.deleteSearch(search)
    }

    override fun onProductClick(product: Product) {
        val action = SearchFragmentDirections.actionNavigationSearchToNavigationProductDetail(product.productId)
        findNavController().navigate(action)
    }

    override fun onFavoriteClick(product: Product) {
        when(appContext.getUserType()) {
            "member" -> {
                viewModel.toggleFavorite(appContext.getUserId(), product.productId)
            }
            "guest" -> {
                appContext.showLoginSnackbar(binding.root) {
                    val action = CategoryProductFragmentDirections.actionNavigationCategoryToSignInActivity()
                    findNavController().navigate(action)
                }
            }
        }
    }
}