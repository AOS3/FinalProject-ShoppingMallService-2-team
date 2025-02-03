//package com.nemodream.bangkkujaengi.customer.ui.adapter
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.nemodream.bangkkujaengi.databinding.FragmentNonMemberOrderBinding
//
//class NonMemberOrderFragment: Fragment() {
//    private var _binding: FragmentNonMemberOrderBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentNonMemberOrderBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupListeners()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    private fun setupListeners() {
//        with(binding) {
//            toolbarNonMemberInquiry.setNavigationOnClickListener {
//                findNavController().navigateUp()
//            }
//
//            btnSignUp.setOnClickListener {
//                val action = NonMemberOrderFragmentDirections.actionNavigationNonMemberOrderToSignUpActivity()
//                findNavController().navigate(action)
//            }
//        }
//    }
//
//}