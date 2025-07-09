package com.example.budgetin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.budgetin.data.DatabaseHelper
import com.example.budgetin.data.Repository          // ← import Repository
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
    ): View? = inflater.inflate(R.layout.fragment_add, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        dbHelper = DatabaseHelper(requireContext())

        /* ---------- view binding ---------- */
        spinnerType       = view.findViewById(R.id.spinner_type)
        spinnerCategory   = view.findViewById(R.id.spinner_category)
        etDate            = view.findViewById(R.id.et_date)
        etAmount          = view.findViewById(R.id.et_amount)
        etTitle           = view.findViewById(R.id.et_title)
        etMessage         = view.findViewById(R.id.et_message)
        btnSave           = view.findViewById(R.id.btn_save)
        tvAmountPreview   = view.findViewById(R.id.tv_amount_preview)

        /* ---------- spinner tipe ---------- */
        val types = listOf("Expenses", "Revenue", "Assets")
        spinnerType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                val selectedType = types[pos]
                val categories   = dbHelper.getAllCategories()
                    .filter { it.type.equals(selectedType, ignoreCase = true) }

                categoryIdList = categories.map { it.id }
                spinnerCategory.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name }
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }

        setupAmountPreview(etAmount, tvAmountPreview)

        /* ---------- DatePicker ---------- */
        etDate.apply {
            isFocusable = false; isClickable = true
            setOnClickListener {
                val cal   = java.util.Calendar.getInstance()
                val dlg   = android.app.DatePickerDialog(
                    requireContext(),
                    { _, y, m, d -> setText(String.format("%04d-%02d-%02d", y, m + 1, d)) },
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                )
                dlg.show()
            }
        }

        /* ---------- tombol SIMPAN ---------- */
        btnSave.setOnClickListener {

            /* validasi field */
            val date       = etDate.text.toString().trim()
            val amountStr  = etAmount.text.toString().replace("[^\\d]".toRegex(), "")
            val title      = etTitle.text.toString().trim()
            val message    = etMessage.text.toString().trim()
            val catPos     = spinnerCategory.selectedItemPosition

            if (date.isEmpty() || amountStr.isEmpty() || title.isEmpty() ||
                catPos == AdapterView.INVALID_POSITION) {
                Toast.makeText(requireContext(), "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categoryIdList.isEmpty() || catPos !in categoryIdList.indices) {
                Toast.makeText(requireContext(), "Kategori tidak tersedia!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /* data transaksi */
            val categoryId = categoryIdList[catPos]
            val amount     = amountStr.toDouble()
            val typeDb     = when (spinnerType.selectedItem.toString()) {
                "Expenses" -> "expense"
                "Revenue"  -> "income"
                "Assets"   -> "goal"
                else       -> "expense"
            }

            /* saldo setelah transaksi */
            val currentBalance   = Repository.getCurrentBalance(requireContext())
            val predictedBalance = when (typeDb) {
                "expense" -> currentBalance - amount
                "income"  -> currentBalance + amount
                else      -> currentBalance          // goal tidak mengubah saldo kas
            }

            /* preferensi budget alert */
            val prefs     = requireContext().getSharedPreferences("BudgetInPrefs", Context.MODE_PRIVATE)
            val alertOn   = prefs.getBoolean("budget_enabled", true)
            val threshold = prefs.getString("budget_amount", "")?.toDoubleOrNull()

            /* fungsi penyimpanan */
            fun doSave() {
                dbHelper.insertTransaction(
                    Transaction(
                        id         = 0,
                        title      = title,
                        amount     = amount,
                        date       = date,
                        type       = typeDb,
                        categoryId = categoryId,
                        goalId     = null,
                        message    = message
                    )
                )
                Toast.makeText(requireContext(), "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                etDate.text.clear(); etAmount.text.clear(); etTitle.text.clear(); etMessage.text.clear()
            }

            /* tampilkan alert bila perlu */
            if (alertOn && threshold != null && predictedBalance < threshold) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Peringatan Budget")
                    .setMessage(
                        "Transaksi ini akan membuat saldo Anda menjadi " +
                                "Rp${predictedBalance.toInt()}, di bawah batas Rp${threshold.toInt()}.\n" +
                                "Tetap lanjutkan?"
                    )
                    .setPositiveButton("Lanjutkan") { _, _ -> doSave() }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                doSave()
            }
        }
    }

    /* ---------- preview format rupiah ---------- */
    private fun setupAmountPreview(etAmount: EditText, tvPreview: TextView) {
        etAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val clean = s.toString().replace("[^\\d]".toRegex(), "")
                val formatted = if (clean.isEmpty()) {
                    "Rp0,00"
                } else {
                    val parsed = clean.toLong()
                    val sym    = DecimalFormatSymbols().apply {
                        groupingSeparator = '.'
                        decimalSeparator  = ','
                    }
                    "Rp${DecimalFormat("#,###", sym).format(parsed)},00"
                }
                tvPreview.text = formatted
            }
        })
    }
}
