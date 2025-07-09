package com.example.budgetin

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetin.data.DatabaseHelper
import com.example.budgetin.data.model.Transaction
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class EditTransactionActivity : AppCompatActivity() {
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
    private var transactionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_edit_transaction)
        dbHelper = DatabaseHelper(this)
        spinnerType = findViewById(R.id.spinner_type)
        spinnerCategory = findViewById(R.id.spinner_category)
        etDate = findViewById(R.id.et_date)
        etAmount = findViewById(R.id.et_amount)
        etTitle = findViewById(R.id.et_title)
        etMessage = findViewById(R.id.et_message)
        tvAmountPreview = findViewById(R.id.tv_amount_preview)
        btnSave = Button(this) // Placeholder, will be replaced below
        // Cari button simpan secara dinamis (jika layout diubah)
        val btnSaveId = resources.getIdentifier("btn_save", "id", packageName)
        if (btnSaveId != 0) btnSave = findViewById(btnSaveId)

        // Ambil data transaksi dari intent
        @Suppress("DEPRECATION")
        val transaction = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("transaction", Transaction::class.java)
        } else {
            @Suppress("UNCHECKED_CAST")
            intent.getSerializableExtra("transaction") as? Transaction
        }
        if (transaction == null) {
            Toast.makeText(this, "Data transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        transactionId = transaction.id

        // Setup spinner tipe transaksi
        val types = listOf("Expenses", "Revenue", "Assets")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
        val typeDbToUi = mapOf("expense" to "Expenses", "income" to "Revenue", "goal" to "Assets")
        val typeUiToDb = mapOf("Expenses" to "expense", "Revenue" to "income", "Assets" to "goal")
        val typeUi = typeDbToUi[transaction.type] ?: "Expenses"
        spinnerType.setSelection(types.indexOf(typeUi))

        // Helper untuk update kategori sesuai tipe
        fun updateCategorySpinner(selectedType: String, selectedCategoryId: Int?) {
            val categories = dbHelper.getAllCategories().filter { it.type.equals(selectedType, ignoreCase = true) }
            val categoryNames = categories.map { it.name }
            categoryIdList = categories.map { it.id }
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = categoryAdapter
            // Set kategori sesuai data
            if (selectedCategoryId != null) {
                val idx = categoryIdList.indexOf(selectedCategoryId)
                if (idx >= 0) spinnerCategory.setSelection(idx)
            }
        }
        // Set kategori awal
        updateCategorySpinner(typeUi, transaction.categoryId)
        // Update kategori saat tipe diubah
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedType = types[position]
                updateCategorySpinner(selectedType, null)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Isi field lain
        etTitle.setText(transaction.title)
        etAmount.setText(transaction.amount.toLong().toString())
        etDate.setText(transaction.date)
        etMessage.setText("") // Tidak ada field message di Transaction, bisa dikosongkan
        // Preview amount
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
                tvAmountPreview.text = formatted
            }
        })
        // DatePicker untuk tanggal
        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(
                this,
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
                Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categoryIdList.isEmpty() || categoryPosition < 0 || categoryPosition >= categoryIdList.size) {
                Toast.makeText(this, "Kategori tidak tersedia!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val categoryId = categoryIdList[categoryPosition]
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val typeDb = typeUiToDb[spinnerType.selectedItem.toString()] ?: "expense"
            val updated = transaction.copy(
                title = title,
                amount = amount,
                date = date,
                type = typeDb,
                categoryId = categoryId,
                message = message
            )
            dbHelper.updateTransaction(updated)
            Toast.makeText(this, "Transaksi berhasil diupdate!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
        val btnBackId = resources.getIdentifier("btn_back", "id", packageName)
        if (btnBackId != 0) {
            val btnBack = findViewById<ImageButton>(btnBackId)
            btnBack.setOnClickListener { finish() }
        }
    }

    companion object {
        /**
         * Untuk migrasi ke Activity Result API:
         * - Panggil EditTransactionActivity dari Activity/Fragment pemanggil menggunakan registerForActivityResult.
         * - Jangan gunakan startActivityForResult lagi.
         * Contoh pemanggilan modern ada di dokumentasi Android Activity Result API.
         */
        fun start(activity: AppCompatActivity, transaction: Transaction) {
            val intent = Intent(activity, EditTransactionActivity::class.java)
            intent.putExtra("transaction", transaction)
            // Deprecated: activity.startActivityForResult(intent, 1001)
            // Migrasi: gunakan ActivityResultLauncher dari pemanggil
            activity.startActivity(intent) // Sementara, tanpa result
        }
    }
} 