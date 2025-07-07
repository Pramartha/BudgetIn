package com.example.budgetin

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class ChartFragment : Fragment(R.layout.fragment_chart) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChart = view.findViewById<PieChart>(R.id.pieChart)

        /* ---------- DATA ---------- */
        val entries = listOf(
            PieEntry(10f, "Income"),
            PieEntry(90f, "Outcome")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#007BFF"),  // biru
                Color.parseColor("#FF1A1A")   // merah
            )
            valueTextColor = Color.WHITE
            valueTextSize = 18f
            sliceSpace = 0f
            valueFormatter = PercentFormatter(pieChart) // "10%"
        }

        pieChart.data = PieData(dataSet)

        /* ---------- TAMPILAN ---------- */
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)

            maxAngle = 180f         // pie setengah
            rotationAngle = 180f    // mulai dari bawah

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                textColor = Color.BLACK
            }

            // cegah terpotong
            setExtraOffsets(5f, 5f, 5f, -40f)

            animateY(700)           // animasi lembut
            invalidate()
        }
    }
}
