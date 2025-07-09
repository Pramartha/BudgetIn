package com.example.budgetin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.budgetin.R
import android.widget.ImageButton

class FontSettingsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_font_settings, container, false)
        val prefs = requireContext().getSharedPreferences("BudgetInPrefs", Context.MODE_PRIVATE)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBarFontSize)
        val tvFontSize = view.findViewById<TextView>(R.id.tvFontSizeValue)


        seekBar.progress = prefs.getInt("font_size", 16)
        tvFontSize.text = "${seekBar.progress}sp"
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvFontSize.text = "${progress}sp"
                prefs.edit().putInt("font_size", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return view
    }
} 