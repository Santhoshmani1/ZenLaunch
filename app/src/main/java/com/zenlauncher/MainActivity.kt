package com.zenlauncher

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.statusBarColor = Color.BLACK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }

        viewPager = ViewPager2(this).apply {
            id = View.generateViewId()
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = ScreenSlidePagerAdapter(this@MainActivity)

            // Set to HomeFragment on fresh launch
            if (savedInstanceState == null) {
                setCurrentItem(0, false)
            }
        }

        setContentView(viewPager)

        // Back button handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem != 0) {
                    viewPager.currentItem = 0
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Always show HomeFragment on screen unlock
        if (::viewPager.isInitialized && viewPager.currentItem != 0) {
            viewPager.setCurrentItem(0, false)
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> HomeFragment()
            1 -> AppListFragment()
            else -> HomeFragment()
        }
    }
}
