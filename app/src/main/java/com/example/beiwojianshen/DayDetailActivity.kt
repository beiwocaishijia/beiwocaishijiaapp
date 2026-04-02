package com.example.beiwojianshen

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.beiwojianshen.data.DateKeys
import com.example.beiwojianshen.ui.WallpaperApplier
import com.example.beiwojianshen.ui.day.CardioFragment
import com.example.beiwojianshen.ui.day.TrainingFragment
import com.example.beiwojianshen.ui.day.WeightFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DayDetailActivity : AppCompatActivity() {
    private lateinit var wallpaperImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_detail)

        wallpaperImage = findViewById(R.id.wallpaperImage)
        WallpaperApplier.apply(this, wallpaperImage)

        val date = intent.getStringExtra(EXTRA_DATE) ?: DateKeys.today()
        title = date

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int) = when (position) {
                0 -> TrainingFragment.newInstance(date)
                1 -> CardioFragment.newInstance(date)
                else -> WeightFragment.newInstance(date)
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "训练"
                1 -> "有氧"
                else -> "体重"
            }
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        WallpaperApplier.apply(this, wallpaperImage)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_day_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_charts -> {
                startActivity(Intent(this, ChartsActivity::class.java))
                true
            }
            R.id.action_exercises -> {
                startActivity(Intent(this, ExercisesActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_DATE = "extra_date"
    }
}
