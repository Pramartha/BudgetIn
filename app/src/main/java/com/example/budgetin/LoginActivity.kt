package com.example.budgetin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        val prefs = getSharedPreferences("BudgetInPrefs", MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", null)

        btnLogin.setOnClickListener {
            val input = etPassword.text.toString().trim()
            if (savedPin.isNullOrEmpty()) {
                Toast.makeText(this, "Belum ada PIN terdaftar! Silakan buat PIN di menu Settings.", Toast.LENGTH_SHORT).show()
            } else if (input.length != 5) {
                Toast.makeText(this, "PIN harus 5 digit!", Toast.LENGTH_SHORT).show()
            } else if (input == savedPin) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "PIN salah!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}