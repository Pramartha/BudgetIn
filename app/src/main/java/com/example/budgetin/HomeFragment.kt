package com.example.budgetin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetin.R
import com.example.budgetin.databinding.FragmentHomeBinding
import com.example.budgetin.data.model.Transaction


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Atur data goals
        binding.tvGoalTitle.text = "Goals : McLaren"
        binding.progressGoal.progress = 30
        binding.tvGoalTarget.text = "Rp.10.000.000,00"

        //Atur data ringkasan Pemasukan-Pengeluaran
        binding.tvTotalAmount.text = "Rp.7.500.000"
        binding.tvIncome.text = "Rp.10.000.000"
        binding.tvExpense.text = "Rp.2.500.000"

        val dummyTransactions = listOf(
            Transaction(1, "Gaji Bulanan", 5000000.0, "2025-06-01", "income", categoryId = null, goalId = null),
            Transaction(2, "Belanja", 1500000.0, "2025-06-05", "expense", categoryId = null, goalId = null),
            Transaction(3, "Freelance", 2500000.0, "2025-06-10", "income", categoryId = null, goalId = null),
            Transaction(4, "Makan", 500000.0, "2025-06-12", "expense", categoryId = null, goalId = null)
        )

        // Inisialisasi RecyclerView
        val adapter = TransactionAdapter(dummyTransactions)
        binding.rvRecentTransactions.adapter = adapter
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        val db = com.example.budgetin.data.DatabaseHelper(requireContext())
//        val transactions = db.getAllTransactions()
//
//        val adapter = TransactionAdapter(transactions)
//        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvRecentTransactions.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}