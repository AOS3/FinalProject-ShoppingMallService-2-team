package com.nemodream.bangkkujaengi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount.FindInfoActivity
import com.nemodream.bangkkujaengi.customer.ui.viewmodel.SignInViewModel
import com.nemodream.bangkkujaengi.databinding.ActivitySignInBinding
import com.nemodream.bangkkujaengi.utils.clearFocusOnTouchOutside
import com.nemodream.bangkkujaengi.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val signInViewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkLoginState()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
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
                showSnackBar(binding.root,"아이디와 비밀번호를 모두 입력해주세요.")
            } else {
                // 관리자 계정 확인
                if (id == "admin" && password == "admin*") {
                    saveLoginState("admin", id)
                    navigateToAdminActivity() // AdminActivity로 이동
                } else {
                    signInViewModel.signIn(id, password) // 일반 사용자 로그인
                }
            }
        }

        // 비회원으로 이용하기 버튼 클릭 시 CustomerActivity로 이동
        binding.tvSignInGuestLogin.setOnClickListener {
            signInViewModel.signInAnonymously()
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
                val userType = if (message == "guest") "guest" else "member"
                saveLoginState(userType, documentId)
                navigateToCustomerActivity()
            } else {
                showSnackBar(binding.root, message)
            }
        }
    }

    private fun navigateToCustomerActivity() {
        val intent = Intent(this, CustomerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // 현재 로그인 화면 종료
    }

    private fun navigateToAdminActivity() {
        // AdminActivity로 이동
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        finish() // 현재 로그인 화면 종료
    }

    private fun saveLoginState(userType: String, documentId: String?) {
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userType", userType)
        editor.putString("documentId", documentId) // 회원 ID
        editor.apply()
    }

    private fun checkLoginState() {
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString("userType", null) // 저장된 사용자 유형
        val documentId = sharedPreferences.getString("documentId", null) // 저장된 사용자 ID

        when {
            userType == null -> {
                Log.d("SignInActivity", "이전 로그인 정보 없음: 비회원 상태")
                // 비회원 상태, 로그인 화면 유지
            }
            userType == "member" && documentId != null -> {
                Log.d("SignInActivity", "이전 로그인 정보: 회원 (documentId=$documentId)")
                navigateToCustomerActivity()
            }
            userType == "admin" -> {
                Log.d("SignInActivity", "이전 로그인 정보: 관리자")
                navigateToAdminActivity()
            }
            else -> {
                Log.d("SignInActivity", "로그인 정보가 올바르지 않음: userType=$userType, documentId=$documentId")
                // 로그인 화면 유지
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        clearFocusOnTouchOutside(event) // Activity 확장 함수 호출
        return super.dispatchTouchEvent(event)
    }

}