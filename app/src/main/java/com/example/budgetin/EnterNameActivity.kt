package com.example.budgetin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EnterNameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_name)

        val etName = findViewById<EditText>(R.id.et_name)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val cbUsePassword = findViewById<CheckBox>(R.id.cb_use_password)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)

        cbUsePassword.setOnCheckedChangeListener { _, isChecked ->
            etPassword.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("BudgetInPrefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("username", name)

            if (cbUsePassword.isChecked && password.isNotEmpty()) {
                editor.putString("password", password) // Bisa di-hash kalau perlu
            }

            editor.apply()

            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}