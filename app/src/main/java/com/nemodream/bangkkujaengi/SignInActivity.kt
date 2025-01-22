package com.nemodream.bangkkujaengi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount.FindInfoActivity
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SignInViewModel
import com.nemodream.bangkkujaengi.databinding.ActivitySignInBinding
import com.nemodream.bangkkujaengi.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val signInViewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // 빈 공간 터치 시 키보드 숨김 처리
        binding.root.setOnClickListener {
            binding.root.hideKeyboard()
        }

        // 회원가입 버튼 클릭 시 회원가입 화면으로 이동
        binding.tvSignInSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 클릭 시 처리
        binding.btnSignInLogin.setOnClickListener {
            val id = binding.tfSignInUserId.editText?.text.toString().trim()
            val password = binding.tfSignInUserPw.editText?.text.toString().trim()

            if (id.isEmpty() || password.isEmpty()) {
                showToast("아이디와 비밀번호를 모두 입력해주세요.")
            } else {
                signInViewModel.signIn(id, password)
            }
        }

        // 비회원으로 이용하기 버튼 클릭 시 CustomerActivity로 이동
        binding.tvSignInGuestLogin.setOnClickListener {
            Log.d("SignInActivity", "비회원으로 이용하기")
            navigateToCustomerActivity(isGuest = true, documentId = null)
        }

        // 아이디/비밀번호 찾기 버튼 클릭 시 FindInfoFragment로 이동
        binding.tvSignInForgot.setOnClickListener{
            val intent = Intent(this, FindInfoActivity::class.java)
            startActivity(intent) // FindInfoActivity로 이동
        }
    }

    private fun observeViewModel() {
        // 로그인 결과 관찰
        signInViewModel.loginResult.observe(this) { result ->
            val (success, message, documentId) = result
            if (success) {
                Log.d("SignInActivity", "회원 로그인 성공, 문서 ID: $documentId")
                navigateToCustomerActivity(isGuest = false, documentId = documentId)
            } else {
                showToast(message)
            }
        }
    }

    private fun navigateToCustomerActivity(isGuest: Boolean, documentId: String?) {
        val intent = Intent(this, CustomerActivity::class.java).apply {
            putExtra("isGuest", isGuest)
            putExtra("documentId", documentId)
        }
        startActivity(intent)
        finish() // 현재 로그인 화면 종료
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}