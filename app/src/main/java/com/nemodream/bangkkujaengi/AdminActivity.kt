package com.nemodream.bangkkujaengi

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.nemodream.bangkkujaengi.databinding.ActivityAdminBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminActivity : AppCompatActivity() {
    private val binding: ActivityAdminBinding by lazy { ActivityAdminBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // SharedPreferences에서 사용자 상태 로드
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString("userType", "guest")
        val documentId = sharedPreferences.getString("documentId", null)

        // 사용자 상태 로그 출력
        Log.d("login", "사용자 유형: $userType, 문서 ID: $documentId")

        setupBackPressHandler()
    }

    /**
     * 뒤로가기 버튼 핸들러를 설정합니다.
     * Fragment 백스택이 있는 경우 이전 화면으로 이동하고,
     * 없는 경우 액티비티를 종료합니다.
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.admin_container) as NavHostFragment

            with(navHostFragment.navController) {
                if (!popBackStack()) {
                    finish()
                }
            }
        }
    }
}