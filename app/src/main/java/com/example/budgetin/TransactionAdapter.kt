package com.example.budgetin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetin.data.model.Transaction
import com.example.budgetin.data.DatabaseHelper
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(
    private val transactionList: List<Transaction>,
    private val listener: OnTransactionActionListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface OnTransactionActionListener {
        fun onEdit(transaction: Transaction)
        fun onDelete(transaction: Transaction)
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val btnEdit: View = itemView.findViewById(R.id.btn_edit)
        val btnDelete: View = itemView.findViewById(R.id.btn_delete)
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

        // Mapping tipe
        val typeLabel = when (transaction.type) {
            "income" -> "Revenue"
            "expense" -> "Expense"
            "goal" -> "Assets"
            else -> transaction.type
        }
        holder.tvType.text = typeLabel

        // Mapping kategori
        val db = DatabaseHelper(holder.itemView.context)
        val categoryName = transaction.categoryId?.let { id ->
            db.getAllCategories().find { it.id == id }?.name
        } ?: "-"
        holder.tvCategory.text = categoryName

        // Pesan
        if (!transaction.message.isNullOrBlank()) {
            holder.tvMessage.text = transaction.message
            holder.tvMessage.visibility = View.VISIBLE
        } else {
            holder.tvMessage.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener {
            listener.onEdit(transaction)
        }
        holder.btnDelete.setOnClickListener {
            listener.onDelete(transaction)
        }

        // Terapkan font global
        FontUtil.applyFontSettings(holder.itemView.context, holder.tvTitle)
        FontUtil.applyFontSettings(holder.itemView.context, holder.tvAmount)
        FontUtil.applyFontSettings(holder.itemView.context, holder.tvDate)
        FontUtil.applyFontSettings(holder.itemView.context, holder.tvType)
        FontUtil.applyFontSettings(holder.itemView.context, holder.tvCategory)
        FontUtil.applyFontSettings(holder.itemView.context, holder.tvMessage)
    }

    override fun getItemCount(): Int = transactionList.size
} 