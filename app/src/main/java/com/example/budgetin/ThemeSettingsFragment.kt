package com.example.budgetin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.example.budgetin.R

class ThemeSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_theme_settings, container, false)

        // Set back button click listener
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Set up radio button group for theme colors
        val rbBlue = view.findViewById<RadioButton>(R.id.rbBlue)
        val rbGreen = view.findViewById<RadioButton>(R.id.rbGreen)
        val rbOrange = view.findViewById<RadioButton>(R.id.rbOrange)

        // Set up radio button listeners
        rbBlue.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbGreen.isChecked = false
                rbOrange.isChecked = false
                // Apply blue theme
            }
        }

        rbGreen.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbBlue.isChecked = false
                rbOrange.isChecked = false
                // Apply green theme
            }
        }

        rbOrange.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbBlue.isChecked = false
                rbGreen.isChecked = false
                // Apply orange theme
            }
        }

        return view
    }
}