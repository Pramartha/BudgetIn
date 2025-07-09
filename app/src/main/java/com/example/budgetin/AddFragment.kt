package com.example.budgetin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.budgetin.R
import com.example.budgetin.data.DatabaseHelper
import com.example.budgetin.data.model.Transaction
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class AddFragment : Fragment() {
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDate: EditText
    private lateinit var etAmount: EditText
    private lateinit var etTitle: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnSave: Button
    private lateinit var tvAmountPreview: TextView

    private lateinit var dbHelper: DatabaseHelper
    private var categoryIdList: List<Int> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dbHelper = DatabaseHelper(requireContext())
        spinnerType = view.findViewById(R.id.spinner_type)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        etDate = view.findViewById(R.id.et_date)
        etAmount = view.findViewById(R.id.et_amount)
        etTitle = view.findViewById(R.id.et_title)
        etMessage = view.findViewById(R.id.et_message)
        btnSave = view.findViewById(R.id.btn_save)
        tvAmountPreview = view.findViewById(R.id.tv_amount_preview)

        // Setup spinner tipe transaksi
        val types = listOf("Expenses", "Revenue", "Assets")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        // Update kategori saat tipe transaksi dipilih
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedType = types[position]
                // Ambil kategori dari database sesuai tipe
                val categories = dbHelper.getAllCategories().filter { it.type.equals(selectedType, ignoreCase = true) }
                val categoryNames = categories.map { it.name }
                categoryIdList = categories.map { it.id }
                val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = categoryAdapter
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        setupAmountPreview(etAmount, tvAmountPreview)

        // DatePicker untuk tanggal
        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val datePicker = android.app.DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    etDate.setText(dateStr)
                },
                year, month, day
            )
            datePicker.show()
        }

        btnSave.setOnClickListener {
            val date = etDate.text.toString().trim()
            val amountStr = etAmount.text.toString().replace("[^\\d]".toRegex(), "")
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()
            val categoryPosition = spinnerCategory.selectedItemPosition

            if (date.isEmpty() || amountStr.isEmpty() || title.isEmpty() || categoryPosition == AdapterView.INVALID_POSITION) {
                Toast.makeText(requireContext(), "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categoryIdList.isEmpty() || categoryPosition < 0 || categoryPosition >= categoryIdList.size) {
                Toast.makeText(requireContext(), "Kategori tidak tersedia!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val categoryId = categoryIdList[categoryPosition]
            val amount = amountStr.toDoubleOrNull() ?: 0.0

            // Mapping tipe transaksi ke value yang diterima database
            val typeDb = when (spinnerType.selectedItem.toString()) {
                "Expenses" -> "expense"
                "Revenue" -> "income"
                "Assets" -> "goal"
                else -> "expense"
            }

            // Simpan transaksi ke database
            dbHelper.insertTransaction(
                com.example.budgetin.data.model.Transaction(
                    id = 0,
                    title = title,
                    amount = amount,
                    date = date,
                    type = typeDb,
                    categoryId = categoryId,
                    goalId = null,
                    message = message
                )
            )

            Toast.makeText(requireContext(), "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
            etDate.text.clear()
            etAmount.text.clear()
            etTitle.text.clear()
            etMessage.text.clear()
        }
    }

    private fun setupAmountPreview(etAmount: EditText, tvPreview: TextView) {
        etAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
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