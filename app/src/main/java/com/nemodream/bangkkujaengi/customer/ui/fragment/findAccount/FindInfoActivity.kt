package com.nemodream.bangkkujaengi.customer.ui.fragment.findAccount

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nemodream.bangkkujaengi.R
import com.nemodream.bangkkujaengi.databinding.ActivityFindInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_find_info, FindInfoFragment())
                .commit()
        }

    }
}
