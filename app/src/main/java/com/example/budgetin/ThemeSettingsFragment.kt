package com.example.budgetin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.example.budgetin.R
import android.widget.ImageButton

class ThemeSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_theme_settings, container, false)
        val prefs = requireContext().getSharedPreferences("BudgetInPrefs", Context.MODE_PRIVATE)
        val rbBlue = view.findViewById<RadioButton>(R.id.rbBlue)
        val rbGreen = view.findViewById<RadioButton>(R.id.rbGreen)
        val rbOrange = view.findViewById<RadioButton>(R.id.rbOrange)
        val theme = prefs.getString("theme_color", "blue")
        rbBlue.isChecked = theme == "blue"
        rbGreen.isChecked = theme == "green"
        rbOrange.isChecked = theme == "orange"
        rbBlue.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs.edit().putString("theme_color", "blue").apply()
            }
        }
        rbGreen.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs.edit().putString("theme_color", "green").apply()
            }
        }
        rbOrange.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs.edit().putString("theme_color", "orange").apply()
            }
        }
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return view
    }
} 
