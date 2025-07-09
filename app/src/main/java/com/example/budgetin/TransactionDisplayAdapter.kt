package com.example.budgetin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetin.R
import java.text.NumberFormat
import java.util.*

// Data class untuk tampilan Home
 data class TransactionDisplay(
    val title: String,
    val amount: Double,
    val date: String,
    val type: String,
    val categoryName: String
)

class TransactionDisplayAdapter(
    private val transactionList: List<TransactionDisplay>
) : RecyclerView.Adapter<TransactionDisplayAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
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
        holder.tvCategory.text = transaction.categoryName
        holder.tvType.text = transaction.type
        val formattedAmount = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(transaction.amount)
        holder.tvAmount.text = formattedAmount
        val colorResId = if (transaction.type.equals("Revenue", true)) R.color.green else R.color.red
        val color = ContextCompat.getColor(holder.itemView.context, colorResId)
        holder.tvAmount.setTextColor(color)
    }

    override fun getItemCount(): Int = transactionList.size
} 