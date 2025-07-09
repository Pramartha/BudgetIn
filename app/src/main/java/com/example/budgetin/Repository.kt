package com.example.budgetin.data

import android.content.Context

object Repository {

    /**
     * Hitung saldo terkini langsung dari database.
     * @return income – expense
     */
    fun getCurrentBalance(context: Context): Double {
        val db = DatabaseHelper(context.applicationContext)
        val transactions = db.getAllTransactions()

        val income  = transactions.filter { it.type == "income"  }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }

        return income - expense
    }
}
