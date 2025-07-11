package com.zenlauncher

import android.content.Intent
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
import com.zenlauncher.ui.screens.AppListFragment
import com.zenlauncher.ui.screens.HomeFragment

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
            adapter = ScreenSlidePagerAdapter(this@MainActivity)

            // Always start with HomeFragment
            setCurrentItem(0, false)
        }

        setContentView(viewPager)

        // Back button handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem != 0) {
                    viewPager.currentItem = 0
                } else {
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (::viewPager.isInitialized && viewPager.currentItem != 0) {
            viewPager.setCurrentItem(0, false)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (::viewPager.isInitialized && viewPager.currentItem != 0) {
            viewPager.setCurrentItem(0, false)
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        private val fragments = mapOf(
            0 to HomeFragment(),
            1 to AppListFragment()
        )

        override fun getItemCount() = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position] ?: HomeFragment()
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun containsItem(itemId: Long): Boolean {
            return itemId in fragments.keys.map { it.toLong() }
        }
    }
}
