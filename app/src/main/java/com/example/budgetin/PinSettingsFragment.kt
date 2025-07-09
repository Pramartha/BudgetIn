package com.example.budgetin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.budgetin.R
import android.widget.ImageButton

class PinSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pin_settings, container, false)
        val prefs = requireContext().getSharedPreferences("BudgetInPrefs", Context.MODE_PRIVATE)
        val etCurrentPin = view.findViewById<EditText>(R.id.etCurrentPin)
        val etNewPin = view.findViewById<EditText>(R.id.etNewPin)
        val etConfirmPin = view.findViewById<EditText>(R.id.etConfirmPin)
        val btnUpdatePin = view.findViewById<Button>(R.id.btnUpdatePin)
        btnUpdatePin.setOnClickListener {
            val currentPin = etCurrentPin.text.toString()
            val newPin = etNewPin.text.toString()
            val confirmPin = etConfirmPin.text.toString()
            val savedPin = prefs.getString("user_pin", "") ?: ""
            if (savedPin.isNotEmpty() && currentPin != savedPin) {
                etCurrentPin.error = "PIN lama salah"
                return@setOnClickListener
            }
            if (newPin.length != 5) {
                etNewPin.error = "PIN harus 5 digit"
                return@setOnClickListener
            }
            if (newPin != confirmPin) {
                etConfirmPin.error = "Konfirmasi PIN tidak sama"
                return@setOnClickListener
            }
            prefs.edit().putString("user_pin", newPin).apply()
            Toast.makeText(requireContext(), "PIN berhasil diupdate", Toast.LENGTH_SHORT).show()
            etCurrentPin.text.clear()
            etNewPin.text.clear()
            etConfirmPin.text.clear()
        }
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return view
    }
} 