package com.jer.kpuapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jer.kpuapp.R
import com.jer.kpuapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFormEntry.setOnClickListener {
            startActivity(Intent(this, FormEntryActivity::class.java))

        }

        binding.btnLihatData.setOnClickListener {
            startActivity(Intent(this, DataPemilihActivity::class.java))
        }

    }
}