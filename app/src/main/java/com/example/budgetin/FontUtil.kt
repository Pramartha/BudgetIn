package com.example.budgetin

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView

object FontUtil {
    fun applyFontSettings(context: Context, textView: TextView) {
        val prefs = context.getSharedPreferences("BudgetInPrefs", Context.MODE_PRIVATE)
        val fontSize = prefs.getInt("font_size", 14)
        val isBold = prefs.getBoolean("font_bold", false)
        val fontFamily = prefs.getString("font_family", "sans-serif")
        textView.textSize = fontSize.toFloat()
        val style = if (isBold) Typeface.BOLD else Typeface.NORMAL
        textView.typeface = Typeface.create(fontFamily, style)
    }
}