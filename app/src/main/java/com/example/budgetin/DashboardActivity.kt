package com.example.budgetin

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    }
}