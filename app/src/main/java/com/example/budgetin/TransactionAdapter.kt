package com.example.budgetin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetin.data.model.Transaction
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(
    private val transactionList: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.tvTitle.text = transaction.title
        holder.tvDate.text = transaction.date

        // Format ke dalam format mata uang Rupiah
        val formattedAmount = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(transaction.amount)
        holder.tvAmount.text = formattedAmount

        // Set warna teks berdasarkan tipe transaksi
        val colorResId = if (transaction.type == "income") R.color.green else R.color.red
        val color = ContextCompat.getColor(holder.itemView.context, colorResId)
        holder.tvAmount.setTextColor(color)
    }

    override fun getItemCount(): Int = transactionList.size
}
