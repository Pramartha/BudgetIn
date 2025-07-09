package com.example.budgetin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class EnterNameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_name)

        val etName = findViewById<EditText>(R.id.et_name)
        val cbUsePin = findViewById<CheckBox>(R.id.cb_use_pin)
        val etPin = findViewById<EditText>(R.id.et_pin)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)

        cbUsePin.setOnCheckedChangeListener { _, isChecked ->
            etPin.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val pin = etPin.text.toString().trim()
            val usePin = cbUsePin.isChecked

            if (name.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (usePin && pin.length != 5) {
                Toast.makeText(this, "PIN harus 5 digit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("BudgetInPrefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("username", name)
            editor.putBoolean("has_pin", usePin)
            if (usePin) editor.putString("user_pin", pin) else editor.remove("user_pin")
            editor.apply()

            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}