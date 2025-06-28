package com.example.budgetin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("BudgetInPrefs", MODE_PRIVATE)
            val username = prefs.getString("username", null)
            val password = prefs.getString("password", null)

            val next = if (username == null) {
                EnterNameActivity::class.java
            } else if (password != null) {
                LoginActivity::class.java
            } else {
                DashboardActivity::class.java
            }

            startActivity(Intent(this, next))
            finish()
        }, 2000)
    }
}
