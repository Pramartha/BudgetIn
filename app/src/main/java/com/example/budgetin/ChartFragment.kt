package com.example.budgetin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetin.data.DatabaseHelper
import com.example.budgetin.data.model.Category
import com.example.budgetin.data.model.Transaction
import com.example.budgetin.databinding.FragmentChartBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.widget.LinearLayout
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetin.EditTransactionActivity

class ChartFragment : Fragment() {
    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var pieChart: PieChart
    private lateinit var adapter: TransactionAdapter
    private var filterType: String = "ALL" // ALL, INCOME, OUTCOME, ASSET

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        setupChart()
        setupFilter()
        return binding.root
    }

    private fun setupChart() {
        pieChart = PieChart(requireContext())
        binding.chartContainer.removeAllViews()
        binding.chartContainer.addView(pieChart)
        pieChart.layoutParams.height = 600
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.legend.isEnabled = false // Legend manual
        pieChart.maxAngle = 180f // Setengah lingkaran
        pieChart.rotationAngle = 180f // Buka ke atas
        pieChart.setDrawEntryLabels(false)
    }

    private fun setupFilter() {
        binding.tvFilterTitle.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.tvFilterTitle)
            popup.menu.add("All Transaction")
            popup.menu.add("Income")
            popup.menu.add("Outcome")
            popup.menu.add("Asset")
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "All Transaction" -> filterType = "ALL"
                    "Income" -> filterType = "INCOME"
                    "Outcome" -> filterType = "OUTCOME"
                    "Asset" -> filterType = "ASSET"
                }
                binding.tvFilterTitle.text = item.title
                updateChartAndList()
                true
            }
            popup.show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateChartAndList()
    }

    private fun updateChartAndList() {
        val db = DatabaseHelper(requireContext())
        val transactions = db.getAllTransactions()
        val categories = db.getAllCategories().associateBy { it.id }
        val filtered = when (filterType) {
            "INCOME" -> transactions.filter { it.type.equals("income", true) }
            "OUTCOME" -> transactions.filter { it.type.equals("expense", true) }
            "ASSET" -> transactions.filter { it.type.equals("goal", true) }
            else -> transactions
        }.sortedByDescending { it.id }
        // Pie chart data
        val pieEntries = mutableListOf<PieEntry>()
        val colorList = mutableListOf<Int>()
        val labelList = mutableListOf<Pair<String, Int>>()
        if (filterType == "ALL") {
            val income = transactions.filter { it.type.equals("income", true) }.sumOf { it.amount }
            val outcome = transactions.filter { it.type.equals("expense", true) }.sumOf { it.amount }
            val asset = transactions.filter { it.type.equals("goal", true) }.sumOf { it.amount }
            if (income > 0) { pieEntries.add(PieEntry(income.toFloat(), "Income")); colorList.add(Color.parseColor("#1976D2")); labelList.add("Income" to Color.parseColor("#1976D2")) }
            if (outcome > 0) { pieEntries.add(PieEntry(outcome.toFloat(), "Outcome")); colorList.add(Color.parseColor("#E53935")); labelList.add("Outcome" to Color.parseColor("#E53935")) }
            if (asset > 0) { pieEntries.add(PieEntry(asset.toFloat(), "Asset")); colorList.add(Color.parseColor("#43A047")); labelList.add("Asset" to Color.parseColor("#43A047")) }
        } else {
            // Group by category
            val group = filtered.groupBy { it.categoryId }
            group.forEach { (catId, list) ->
                val catName = categories[catId]?.name ?: "Other"
                val total = list.sumOf { it.amount }
                val color = Color.parseColor(randomColor(catName))
                pieEntries.add(PieEntry(total.toFloat(), catName))
                colorList.add(color)
                labelList.add(catName to color)
            }
        }
        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = colorList
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 16f
        dataSet.sliceSpace = 2f
        dataSet.valueFormatter = com.github.mikephil.charting.formatter.PercentFormatter(pieChart)
        dataSet.setDrawValues(true)
        dataSet.setDrawIcons(false)
        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()

        // Update manual legend horizontal, 2 per baris
        binding.legendContainer.removeAllViews()
        android.util.Log.d("ChartLegend", "Jumlah label: ${labelList.size}")
        val context = requireContext()
        val rowParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        labelList.chunked(2).forEach { pairList ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = rowParams
            }
            pairList.forEach { (label, color) ->
                val item = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    layoutParams = params
                    setPadding(0, 8, 0, 8)
                }
                val dot = View(context).apply {
                    setBackgroundResource(android.R.drawable.presence_online)
                    background.setTint(color)
                    val size = 24
                    layoutParams = LinearLayout.LayoutParams(size, size).apply { rightMargin = 12 }
                }
                val tv = android.widget.TextView(context).apply {
                    text = label
                    setTextColor(Color.BLACK)
                    textSize = 16f
                }
                item.addView(dot)
                item.addView(tv)
                row.addView(item)
            }
            binding.legendContainer.addView(row)
        }

        // Update RecyclerView
        adapter = TransactionAdapter(filtered, object : TransactionAdapter.OnTransactionActionListener {
            override fun onEdit(transaction: Transaction) {
                val intent = Intent(requireContext(), EditTransactionActivity::class.java)
                intent.putExtra("transaction", transaction)
                startActivity(intent)
            }
            override fun onDelete(transaction: Transaction) {
                // Hapus transaksi dari database dan refresh
                db.deleteTransaction(transaction.id)
                updateChartAndList()
            }
        })
        binding.rvChartTransactions.adapter = adapter
        binding.rvChartTransactions.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun randomColor(seed: String): String {
        // Simple hash to color
        val colors = listOf("#1976D2", "#E53935", "#43A047", "#FBC02D", "#8E24AA", "#00897B", "#F57C00")
        return colors[Math.abs(seed.hashCode()) % colors.size]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}