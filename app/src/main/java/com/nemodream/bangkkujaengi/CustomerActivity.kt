package com.nemodream.bangkkujaengi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nemodream.bangkkujaengi.databinding.ActivityCustomerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerActivity : AppCompatActivity() {
    private val binding: ActivityCustomerBinding by lazy { ActivityCustomerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}