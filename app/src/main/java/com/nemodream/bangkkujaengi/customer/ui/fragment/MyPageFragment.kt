package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import com.nemodream.bangkkujaengi.databinding.FragmentMyPageBinding

class MyPageFragment : Fragment() {
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        with(binding) {
            // '3000원'을 텍스트 스타일을 볼드체로 변경
            SpannableString(tvMyPageLoginSubTitle.text).apply {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    tvMyPageLoginSubTitle.text.indexOf("3000"),
                    tvMyPageLoginSubTitle.text.indexOf("3000") + "3000".length + 1,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                tvMyPageLoginSubTitle.text = this
            }
        }
    }
}