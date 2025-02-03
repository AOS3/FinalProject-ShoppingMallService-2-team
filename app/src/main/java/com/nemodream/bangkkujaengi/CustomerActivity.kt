package com.nemodream.bangkkujaengi

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.nemodream.bangkkujaengi.databinding.ActivityCustomerBinding
import com.nemodream.bangkkujaengi.utils.getUserType
import com.nemodream.bangkkujaengi.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerActivity : AppCompatActivity() {
    private val binding: ActivityCustomerBinding by lazy {
        ActivityCustomerBinding.inflate(
            layoutInflater
        )
    }
    private var lastBackPress: Long = 0

    // 네비게이션 보여야 되는 화면은 여기 정의해주세요.
    private val showBottomNavDestinations = listOf(
        R.id.navigation_home,
        R.id.navigation_social,
        R.id.navigation_my_page,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // SharedPreferences에서 사용자 상태 로드
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString("userType", "guest") // 기본값: guest
        val documentId = sharedPreferences.getString("documentId", null)

        // 사용자 상태 로그 출력
        when (userType) {
            "admin" -> Log.d("login", "관리자로 이용 중")
            "member" -> Log.d("login", "회원으로 이용 중, 문서 ID: $documentId")
            "guest" -> Log.d("login", "비회원으로 이용 중")
            else -> Log.d("login", "알 수 없는 사용자 상태")
        }

        val navController = setupNavHost()
        setupListeners(navController)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.customer_container) as NavHostFragment
        setupBackPressHandler(navHostFragment)

    }

    /**
     * NavHost를 설정하고 BottomNavigation과 연결
     */
    private fun setupNavHost(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.customer_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.customerBottomNavigation.setupWithNavController(navController)
        return navController
    }


    /**
     * 뒤로가기 버튼 핸들러를 설정
     * Home 화면에서는 두 번 클릭 시 앱이 종료
     * 다른 화면에서는 일반적인 뒤로가기 동작을 수행
     */
    private fun setupBackPressHandler(navHostFragment: NavHostFragment) {
        onBackPressedDispatcher.addCallback(this) {
            val navController = navHostFragment.navController
            handleBackPress(navController)
        }
    }

    /**
     * 뒤로가기 버튼 클릭 시의 동작을 처리
     * Home 화면인 경우 두 번 클릭 로직을 실행하고,
     * 다른 화면에서는 일반적인 뒤로가기를 수행
     */
    private fun handleBackPress(navController: NavController) {
        val currentDestination = navController.currentDestination
        when {
            isHomeFragment(currentDestination) -> handleHomeBackPress()
            !navController.popBackStack() -> finish()
        }
    }

    /**
     * 현재 화면이 Home 화면인지 확인
     */
    private fun isHomeFragment(destination: NavDestination?): Boolean =
        destination?.id == R.id.navigation_home

    /**
     * Home 화면에서의 뒤로가기 버튼 클릭을 처리
     * 첫 번째 클릭 시 안내 메시지를 표시하고,
     * 정해진 시간 내에 두 번째 클릭 시 앱을 종료
     */
    private fun handleHomeBackPress() {
        val currentTime = System.currentTimeMillis()
        if (isFirstBackPressOrTimeExceeded(currentTime)) {
            lastBackPress = currentTime
            showSnackBar(binding.root, BACK_PRESS_MESSAGE)
        } else {
            finish()
        }
    }

    private fun isFirstBackPressOrTimeExceeded(currentTime: Long): Boolean =
        currentTime - lastBackPress > BACK_PRESS_THRESHOLD

    /**
     * 특정 화면에서 BottomNavigation의 표시 여부를 설정
     * Home, Social 화면에서는 표시되고 다른 화면에서는 숨김
     */
    private fun setupListeners(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 비회원일 때 Social 화면 접근 방지
            if (getUserType() == "guest" && destination.id == R.id.navigation_social) {
                // 비회원은 Social 화면에 접근 못하도록 막고, 스낵바 표시
                showSnackBar(binding.root, "비회원은 이 화면에 접근할 수 없습니다.")
                // 현재 Social 화면으로의 이동을 막기 위해 navigateUp()
                navController.navigateUp()
            } else {
                // 네비게이션 화면 표시
                binding.customerBottomNavigation.visibility = when (destination.id) {
                    in showBottomNavDestinations -> View.VISIBLE
                    else -> View.GONE
                }
            }
        }


//        회원일 경우: 네비게이션 바텀 아이템 클릭을 정상 처리
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            if (userType == "member") {
//                binding.customerBottomNavigation.visibility = when (destination.id) {
//                    in showBottomNavDestinations -> View.VISIBLE
//                    else -> View.GONE
//                }
//            }else{
//                when (destination.id) {
//                    R.id.navigation_home -> {binding.customerBottomNavigation.visibility = View.VISIBLE}
//                    R.id.navigation_social -> {
//                        showGuestOnlyDialog()
//                        binding.customerBottomNavigation.visibility = View.GONE }
//                    R.id.navigation_my_page -> {binding.customerBottomNavigation.visibility = View.VISIBLE}
//                }
//            }
//        }
        }

        companion object {
            private const val BACK_PRESS_THRESHOLD = 2000L
            private const val BACK_PRESS_MESSAGE = "뒤로가기 버튼을 한 번 더 누르면 종료됩니다."
        }
    }