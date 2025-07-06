package com.example.budgetin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetin.databinding.FragmentHomeBinding
import com.example.budgetin.data.DatabaseHelper

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TransactionDisplayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        android.util.Log.d("HomeFragment", "onCreateView dipanggil")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        adapter = TransactionDisplayAdapter(emptyList())
        binding.rvRecentTransactions.adapter = adapter
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("HomeFragment", "onResume dipanggil")
        val db = DatabaseHelper(requireContext())
        val transactions = db.getAllTransactions()
        val categories = db.getAllCategories().associateBy { it.id }
        val displayList = transactions.sortedByDescending { it.id }.map { tx ->
            val category = tx.categoryId?.let { categories[it] }
            TransactionDisplay(
                title = tx.title,
                amount = tx.amount,
                date = tx.date,
                type = category?.type ?: "-",
                categoryName = category?.name ?: "-"
            )
        }
        android.util.Log.d("HomeFragment", "Jumlah transaksi: ${displayList.size}")
        adapter = TransactionDisplayAdapter(displayList)
        binding.rvRecentTransactions.adapter = adapter
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())

        // Hitung total, income, dan expense
        val total = transactions.sumOf { if (it.type == "income") it.amount else if (it.type == "expense") -it.amount else 0.0 }
        val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        binding.tvTotalAmount.text = formatRupiah(total)
        binding.tvIncome.text = formatRupiah(income)
        binding.tvExpense.text = formatRupiah(expense)
    }

    private fun formatRupiah(amount: Double): String {
        val localeID = java.util.Locale("in", "ID")
        val format = java.text.NumberFormat.getCurrencyInstance(localeID)
        return format.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}