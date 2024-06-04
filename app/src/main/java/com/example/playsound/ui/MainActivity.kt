package com.example.playsound.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playsound.R
import com.example.playsound.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(R.id.container, PlaySoundFragment())
            .commit()
    }
}