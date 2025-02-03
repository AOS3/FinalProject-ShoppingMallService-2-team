package com.nemodream.bangkkujaengi.customer.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.FragmentSocialFollowingAllBinding
import com.nemodream.bangkkujaengi.customer.ui.adapter.SocialFollowingAllAdapter
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SocialFollowingAllViewModel
import com.nemodream.bangkkujaengi.utils.getUserId
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class SocialFollowingAllFragment : Fragment() {

    private var _binding: FragmentSocialFollowingAllBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext: Context

    private val viewModel: SocialFollowingAllViewModel by viewModels()

    // 어댑터를 멤버 변수로 선언
    private lateinit var socialFollowingAllAdapter: SocialFollowingAllAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialFollowingAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupListeners()
        viewModel.loadFollowingAllMembers(appContext.getUserId())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        // 어댑터 초기화
        socialFollowingAllAdapter = SocialFollowingAllAdapter(
            currentUserDocId = appContext.getUserId(),
            firestore = FirebaseFirestore.getInstance()
        )

        // RecyclerView 설정
        with(binding.rvSocialFollowingAll) {
            layoutManager = LinearLayoutManager(context)
            adapter = socialFollowingAllAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.followingMembers.observe(viewLifecycleOwner, Observer { followingMembers ->
            Log.d("SocialFollowingAllFragment", "Following members: $followingMembers")
            socialFollowingAllAdapter.submitList(followingMembers)
        })
    }

    private fun setupListeners() {
        with(binding) {
            toolbarSocial.setNavigationOnClickListener {
                findNavController().popBackStack(R.id.navigation_social, false)
            }
        }
    }
}