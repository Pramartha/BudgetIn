package com.example.budgetin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetin.databinding.FragmentHomeBinding
import com.example.budgetin.data.DatabaseHelper
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlertDialog
import android.widget.EditText
import android.widget.ImageView
import android.text.Editable
import android.text.TextWatcher
import java.text.DecimalFormat
import com.example.budgetin.data.model.Transaction
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.TextView
import android.widget.ArrayAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetin.EditTransactionActivity
import android.widget.Toast

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TransactionAdapter
    private var transactionList: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        android.util.Log.d("HomeFragment", "onCreateView dipanggil")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        adapter = TransactionAdapter(emptyList(), object : TransactionAdapter.OnTransactionActionListener {
            override fun onEdit(transaction: Transaction) {
                // showEditTransactionDialog(transaction) dihapus, gunakan EditTransactionActivity
            }
            override fun onDelete(transaction: Transaction) {
                showDeleteTransactionDialog(transaction)
            }
        })
        binding.rvRecentTransactions.adapter = adapter
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())

        // Fitur edit goal
        binding.btnEditGoal.setOnClickListener {
            showEditGoalDialog()
        }
        return binding.root
    }

    private fun showEditGoalDialog() {
        val prefs = requireContext().getSharedPreferences("BudgetInPrefs", Context.MODE_PRIVATE)
        val currentName = prefs.getString("goal_name", "McLaren") ?: "McLaren"
        val currentTarget = prefs.getFloat("goal_target", 10000000f)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_goal, null)
        val etName = dialogView.findViewById<EditText>(R.id.etGoalName)
        val etTarget = dialogView.findViewById<EditText>(R.id.etGoalTarget)
        etName.setText(currentName)
        etTarget.setText(DecimalFormat("#,###").format(currentTarget.toLong()))
        // Format angka otomatis saat input
        etTarget.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etTarget.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[.]", "").replace(",", "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toLongOrNull() ?: 0L
                        val formatted = DecimalFormat("#,###").format(parsed)
                        current = formatted
                        etTarget.setText(formatted)
                        etTarget.setSelection(formatted.length)
                    } else {
                        current = ""
                        etTarget.setText("")
                    }
                    etTarget.addTextChangedListener(this)
                }
            }
        })
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Goal")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etName.text.toString().trim()
                val targetStr = etTarget.text.toString().replace("[.]", "").replace(",", "")
                if (name.isBlank()) {
                    etName.error = "Judul goal tidak boleh kosong"
                    return@setPositiveButton
                }
                val target = targetStr.toFloatOrNull() ?: 10000000f
                prefs.edit().putString("goal_name", name).putFloat("goal_target", target).apply()
                updateGoalView()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateGoalView() {
        val prefs = requireContext().getSharedPreferences("BudgetInPrefs", Context.MODE_PRIVATE)
        val name = prefs.getString("goal_name", "McLaren") ?: "McLaren"
        val target = prefs.getFloat("goal_target", 10000000f)
        binding.tvGoalTitle.text = "Goals : $name"
        binding.tvGoalTarget.text = formatRupiah(target.toDouble())
        // Hitung progress dari total (income - expense)
        val db = DatabaseHelper(requireContext())
        val income = db.getAllTransactions().filter { it.type == "income" }.sumOf { it.amount }
        val expense = db.getAllTransactions().filter { it.type == "expense" }.sumOf { it.amount }
        val progress = if (target > 0) (((income - expense) / target) * 100).toInt().coerceAtMost(100) else 0
        binding.progressGoal.progress = progress
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("HomeFragment", "onResume dipanggil")
        val db = DatabaseHelper(requireContext())
        transactionList = db.getAllTransactions().sortedByDescending { it.id }
        adapter = TransactionAdapter(transactionList, object : TransactionAdapter.OnTransactionActionListener {
            override fun onEdit(transaction: Transaction) {
                val intent = Intent(requireContext(), EditTransactionActivity::class.java)
                intent.putExtra("transaction", transaction)
                startActivity(intent)
            }
            override fun onDelete(transaction: Transaction) {
                showDeleteTransactionDialog(transaction)
            }
        })
        binding.rvRecentTransactions.adapter = adapter
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())

        // Hitung total, income, dan expense
        val total = transactionList.sumOf { if (it.type == "income") it.amount else if (it.type == "expense") -it.amount else 0.0 }
        val income = transactionList.filter { it.type == "income" }.sumOf { it.amount }
        val expense = transactionList.filter { it.type == "expense" }.sumOf { it.amount }
        binding.tvTotalAmount.text = formatRupiah(total)
        binding.tvIncome.text = formatRupiah(income)
        binding.tvExpense.text = formatRupiah(expense)

        // Budget Alert Logic
        val prefs = requireContext().getSharedPreferences("BudgetInPrefs", Context.MODE_PRIVATE)
        val budgetAlertEnabled = prefs.getBoolean("budget_alert_enabled", false)
        val goalTarget = prefs.getFloat("goal_target", 10000000f)
        if (budgetAlertEnabled && goalTarget > 0) {
            val threshold = 0.2 * goalTarget
            if (income < threshold) {
                Toast.makeText(requireContext(), "Anda terlalu di bawah budgets", Toast.LENGTH_LONG).show()
            }
        }

        // Tampilkan greeting, nama, tanggal, dan jam
        val username = prefs.getString("username", "User")
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 4..10 -> getString(R.string.greeting_morning)
            in 11..14 -> getString(R.string.greeting_afternoon)
            in 15..18 -> getString(R.string.greeting_evening)
            else -> getString(R.string.greeting_night)
        }
        val hiText = "Hi, $username\n$greeting"
        binding.greetingText.text = hiText

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
        val timeText = timeFormat.format(now.time) + "\n" + dateFormat.format(now.time)
        binding.timeText.text = timeText
        updateGoalView()
    }

    private fun formatRupiah(amount: Double): String {
        val localeID = java.util.Locale("in", "ID")
        val format = java.text.NumberFormat.getCurrencyInstance(localeID)
        return format.format(amount)
    }

    private fun showDeleteTransactionDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Transaksi")
            .setMessage("Yakin ingin menghapus transaksi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                val db = DatabaseHelper(requireContext())
                db.deleteTransaction(transaction.id)
                refreshTransactions()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun refreshTransactions() {
        val db = DatabaseHelper(requireContext())
        transactionList = db.getAllTransactions().sortedByDescending { it.id }
        adapter = TransactionAdapter(transactionList, object : TransactionAdapter.OnTransactionActionListener {
            override fun onEdit(transaction: Transaction) {
                // showEditTransactionDialog(transaction) dihapus, gunakan EditTransactionActivity
            }
            override fun onDelete(transaction: Transaction) {
                showDeleteTransactionDialog(transaction)
            }
        })
        binding.rvRecentTransactions.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}