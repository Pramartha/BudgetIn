package com.example.budgetin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.budgetin.data.Repository

class NotificationSettingsFragment : Fragment() {

    /* ---------- konstanta SharedPreferences ---------- */
    private val PREF_NAME        = "BudgetInPrefs"
    private val KEY_PUSH_NOTIF   = "notif_enabled"
    private val KEY_DAILY_REM    = "daily_enabled"
    private val KEY_BUDGET_ALRT  = "budget_enabled"
    private val KEY_BUDGET_VALUE = "budget_amount"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /* 1. Inflate layout */
        val view = inflater.inflate(R.layout.fragment_notification_settings, container, false)

        /* 2. Bind view */
        // Hanya sisakan switchBudgetAlerts, frm_Allert, btn_save_settingnotif, btn_back
        val swBudget   = view.findViewById<SwitchCompat>(R.id.switchBudgetAlerts)
        val edtBudget  = view.findViewById<EditText>(R.id.frm_Allert)
        val btnSave    = view.findViewById<Button>(R.id.btn_save_settingnotif)
        val btnBack    = view.findViewById<ImageButton>(R.id.btn_back)

        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        swBudget.isChecked = prefs.getBoolean(KEY_BUDGET_ALRT, true)
        edtBudget.setText(prefs.getString(KEY_BUDGET_VALUE, ""))

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        btnSave.setOnClickListener {
            prefs.edit()
                .putBoolean(KEY_BUDGET_ALRT,  swBudget.isChecked)
                .putString (KEY_BUDGET_VALUE, edtBudget.text.toString().trim())
                .apply()
            Toast.makeText(requireContext(), "Pengaturan berhasil disimpan", Toast.LENGTH_SHORT).show()
            val currentBalance = Repository.getCurrentBalance(requireContext())
            checkBudgetAlert(requireContext(), currentBalance)
        }
        return view
    }

    /**
     * Tampilkan dialog jika balance < threshold.
     */
    private fun checkBudgetAlert(context: Context, balance: Double) {
        val prefs     = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val alertOn   = prefs.getBoolean(KEY_BUDGET_ALRT, true)
        val threshold = prefs.getString(KEY_BUDGET_VALUE, "")?.toDoubleOrNull()

        if (alertOn && threshold != null && balance < threshold) {
            AlertDialog.Builder(context)
                .setTitle("Peringatan Budget")
                .setMessage(
                    "Saldo Anda Rp${balance.toInt()} sudah di bawah batas " +
                            "Rp${threshold.toInt()}. Harap periksa pengeluaran Anda.")
                .setPositiveButton("Oke") { d, _ -> d.dismiss() }
                .show()
        }
    }
}
