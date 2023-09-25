package com.example.customtablayoutsample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pagerIndicator = findViewById<PagerIndicator>(R.id.pager_indicator)
        pagerIndicator.onTextClick = { pagerIndicator.position = 0f }
        pagerIndicator.onAudioClick = { pagerIndicator.position = 1f }
    }
}