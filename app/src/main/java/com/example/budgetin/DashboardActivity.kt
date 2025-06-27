package com.example.budgetin

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetin.ui.home.HomeFragment
import com.example.budgetin.ui.home.AddFragment
import com.example.budgetin.ui.home.ChartFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val rootView = findViewById<View>(R.id.main)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Terapkan padding bawah khusus ke bottom_navigation
            bottomNav.setPadding(
                bottomNav.paddingLeft,
                bottomNav.paddingTop,
                bottomNav.paddingRight,
                systemBars.bottom
            )

            // Opsional: tambahkan padding top ke layout utama jika ingin status bar tidak nutup UI
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0 // Jangan tambahkan padding bawah ke root karena sudah diberikan ke bottomNav
            )
            insets
        }

        // Load HomeFragment saat pertama kali dibuka
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        // Listener untuk BottomNavigationView
        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.item_home -> HomeFragment()
                R.id.item_add -> AddFragment()
                R.id.item_chart -> ChartFragment()
                R.id.item_settings -> SettingsFragment()
                else -> null
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }

            true
        }
    }
}