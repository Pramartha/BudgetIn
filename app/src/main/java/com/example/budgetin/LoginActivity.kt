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
        val savedPassword = prefs.getString("password", null)

        btnLogin.setOnClickListener {
            val input = etPassword.text.toString().trim()

            if (input == savedPassword) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                // Salah password
                Toast.makeText(this, "Password salah!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}