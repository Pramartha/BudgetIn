package com.example.budgetin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.budgetin.R
import com.example.budgetin.data.DatabaseHelper
import com.example.budgetin.data.model.Transaction
import java.util.Calendar
import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import java.util.Locale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class AddFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDate: EditText
    private lateinit var etAmount: EditText
    private lateinit var etTitle: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnSave: Button
    private var categoryIdList: List<Int> = emptyList()

    private val categoriesMap = mapOf(
        "Expenses" to listOf("Transportasi", "Makanan", "Skincare", "Hobby", "Urgent", "Accesories"),
        "Revenue" to listOf("Gaji", "Untung Jualan", "Bonus", "Pendapatan Lainnya"),
        "Assets" to listOf("Tabungan", "Investasi", "Properti", "Aset Lainnya")
    )

    private lateinit var spinnerType: Spinner

    // Fungsi ini di luar onViewCreated!
    private fun setupRupiahFormatter(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                if (cleanString.isNotEmpty()) {
                    try {
                        val parsed = cleanString.toLong()
                        val symbols = DecimalFormatSymbols().apply {
                            groupingSeparator = '.'
                            decimalSeparator = ','
                        }
                        val formatter = DecimalFormat("#,###", symbols)
                        val formatted = "Rp${formatter.format(parsed)},00"
                        editText.setText(formatted)
                        // Posisikan kursor sebelum ,00
                        editText.setSelection(formatted.length - 3)
                    } catch (e: Exception) {
                        editText.setText("")
                    }
                } else {
                    editText.setText("")
                }
                isEditing = false
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())
        spinnerType = view.findViewById(R.id.spinner_type)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        etDate = view.findViewById(R.id.et_date)
        etAmount = view.findViewById(R.id.et_amount)
        etTitle = view.findViewById(R.id.et_title)
        etMessage = view.findViewById(R.id.et_message)
        btnSave = view.findViewById(R.id.btn_save)

        // Ambil kategori dari database
        val categories = dbHelper.getAllCategories()
        val categoryNames = categories.map { it.name }
        categoryIdList = categories.map { it.id }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        val types = categoriesMap.keys.toList()
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        // Set kategori sesuai tipe
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedType = types[position]
                val categories = categoriesMap[selectedType] ?: emptyList()
                val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = categoryAdapter
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    etDate.setText(dateStr)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        val tvAmountPreview = view.findViewById<TextView>(R.id.tv_amount_preview)
        setupAmountPreview(etAmount, tvAmountPreview)

        btnSave.setOnClickListener {
            val date = etDate.text.toString().trim()
            val amountStr = etAmount.text.toString().replace("[^\\d]".toRegex(), "")
            val amount = amountStr.toDoubleOrNull()
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()
            val categoryPosition = spinnerCategory.selectedItemPosition
            if (date.isEmpty() || amount == null || title.isEmpty() || categoryPosition == AdapterView.INVALID_POSITION) {
                Toast.makeText(requireContext(), "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val categoryId = categoryIdList[categoryPosition]
            val category = categories[categoryPosition]
            val type = category.type // income/expense
            val transaction = Transaction(
                title = title,
                amount = amount,
                date = date,
                type = type,
                categoryId = categoryId,
                goalId = null // Belum support goal
            )
            val db = dbHelper.writableDatabase
            val values = android.content.ContentValues().apply {
                put("title", transaction.title)
                put("amount", transaction.amount)
                put("date", transaction.date)
                put("type", transaction.type)
                put("category_id", transaction.categoryId)
                put("goal_id", transaction.goalId)
            }
            val result = db.insert("transactions", null, values)
            if (result != -1L) {
                Toast.makeText(requireContext(), "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                etDate.text.clear()
                etAmount.text.clear()
                etTitle.text.clear()
                etMessage.text.clear()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan transaksi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAmountPreview(etAmount: EditText, tvPreview: TextView) {
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                val formatted = if (cleanString.isEmpty()) {
                    "Rp0,00"
                } else {
                    val parsed = cleanString.toLong()
                    val symbols = DecimalFormatSymbols().apply {
                        groupingSeparator = '.'
                        decimalSeparator = ','
                    }
                    val formatter = DecimalFormat("#,###", symbols)
                    "Rp${formatter.format(parsed)},00"
                }
                tvPreview.text = formatted
            }
        })
    }
}